package shamu.company.email.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.email.entity.Email;
import shamu.company.email.event.EmailEvent;
import shamu.company.email.event.EmailStatus;
import shamu.company.email.repository.EmailRepository;
import shamu.company.helpers.EmailHelper;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserService;
import shamu.company.utils.AvatarUtil;
import shamu.company.utils.DateUtil;
import shamu.company.utils.UuidUtil;

@Service
public class EmailService {
  private static final String FRONT_END_ADDRESS = "frontEndAddress";

  private final EmailRepository emailRepository;

  private final TaskScheduler taskScheduler;

  private final EmailHelper emailHelper;

  private final Integer emailRetryLimit;

  private final ITemplateEngine templateEngine;
  private final UserService userService;
  private final AwsHelper awsHelper;
  private final Logger logger = LoggerFactory.getLogger(EmailService.class);
  @Value("${application.systemEmailAddress}")
  private String systemEmailAddress;
  @Value("${application.frontEndAddress}")
  private String frontEndAddress;

  @Autowired
  public EmailService(
      final EmailRepository emailRepository,
      final TaskScheduler taskScheduler,
      final EmailHelper emailHelper,
      @Value("${email.retryLimit}") final Integer emailRetryLimit,
      final ITemplateEngine templateEngine,
      @Lazy final UserService userService,
      final AwsHelper awsHelper) {
    this.emailRepository = emailRepository;
    this.taskScheduler = taskScheduler;
    this.emailHelper = emailHelper;
    this.emailRetryLimit = emailRetryLimit;
    this.templateEngine = templateEngine;
    this.userService = userService;
    this.awsHelper = awsHelper;
  }

  public Email save(final Email email) {
    return emailRepository.save(email);
  }

  public List<Email> findAllUnfinishedTasks() {
    return emailRepository.findAllUnfinishedTasks(emailRetryLimit);
  }

  public void scheduleEmail(final Email email) {
    Timestamp sendDate = email.getSendDate();
    if (sendDate == null) {
      sendDate = Timestamp.valueOf(LocalDateTime.now());
    }
    taskScheduler.schedule(getEmailTask(email), sendDate);
  }

  public void saveAndScheduleEmail(final Email email) {
    save(email);
    scheduleEmail(email);
  }

  private void rescheduleFailedEmail(final Email email) {
    final Integer currentRetryCount = email.getRetryCount() == null ? 0 : email.getRetryCount() + 1;
    email.setRetryCount(currentRetryCount);
    save(email);

    if (email.getRetryCount() >= emailRetryLimit) {
      return;
    }

    final LocalDateTime afterOneHour = LocalDateTime.now().plusHours(1);
    email.setSendDate(Timestamp.valueOf(afterOneHour));
    saveAndScheduleEmail(email);
  }

  public Runnable getEmailTask(final Email email) {
    return () -> {
      try {
        email.setMessageId(UuidUtil.getUuidString());
        emailHelper.send(email);
        email.setSentAt(new Timestamp(new Date().getTime()));
        emailRepository.save(email);
      } catch (final Exception exception) {
        rescheduleFailedEmail(email);
      }
    };
  }

  public Email findFirstByToAndSubjectOrderBySendDateDesc(final String email, final String s) {
    return emailRepository.findFirstByToAndSubjectOrderBySendDateDesc(email, s);
  }

  public String getWelcomeEmail(final Context context) {
    return templateEngine.process("employee_invitation_email.html", context);
  }

  public Context getWelcomeEmailContext(
      final String welcomeMessage, final String resetPasswordToken, final String invitationToken) {
    return getWelcomeEmailContextToEmail(welcomeMessage, resetPasswordToken, invitationToken, "");
  }

  public Context getWelcomeEmailContextToEmail(
      String welcomeMessage,
      final String resetPasswordToken,
      final String invitationToken,
      final String toEmail) {
    final Context context = new Context();
    context.setVariable(FRONT_END_ADDRESS, frontEndAddress);
    final String emailAddress = getEncodedEmailAddress(toEmail);
    String targetLink =
        frontEndAddress + "account/password/" + resetPasswordToken + "/" + invitationToken;
    if (!"".equals(emailAddress)) {
      targetLink += "/" + emailAddress;
    }
    context.setVariable("createPasswordAddress", targetLink);
    welcomeMessage = getFilteredWelcomeMessage(welcomeMessage);
    context.setVariable("welcomeMessage", welcomeMessage);
    return context;
  }

  public Context findWelcomeEmailPreviewContext(
      final User currentUser, final String welcomeEmailPersonalMessage) {
    final Context context = getWelcomeEmailContext(welcomeEmailPersonalMessage, null, null);
    context.setVariable("createPasswordAddress", "#");
    context.setVariable("companyName", currentUser.getCompany().getName());
    return context;
  }

  public String getEncodedEmailAddress(final String emailAddress) {
    if (Strings.isBlank(emailAddress) || !Pattern.matches("^[a-zA-Z0-9@.+]*$", emailAddress)) {
      return "";
    }
    byte[] reverseEmails = StringUtils.reverse(emailAddress).getBytes();
    return Base64.getEncoder().encodeToString(reverseEmails);
  }

  private String getFilteredWelcomeMessage(String welcomeMessage) {
    if (Strings.isBlank(welcomeMessage)) {
      welcomeMessage = "";
    }
    return welcomeMessage
        .replaceAll("href\\s*=\\s*(['\"])\\s*(?!http[s]?).+?\\1", "#")
        .replaceAll("<script(.*)?>.*</script>", "");
  }

  public String getResetPasswordEmail(final String passwordRestToken, final String toEmail) {
    final Context context = new Context();
    context.setVariable(FRONT_END_ADDRESS, frontEndAddress);
    context.setVariable("toEmailAddress", getEncodedEmailAddress(toEmail));
    context.setVariable(
        "passwordResetAddress", String.format("account/reset-password/%s", passwordRestToken));
    return templateEngine.process("password_reset_email.html", context);
  }

  private String getVerifiedEmail(final String changePasswordToken) {
    final Context context = new Context();
    context.setVariable(FRONT_END_ADDRESS, frontEndAddress);
    context.setVariable(
        "changePasswordToken", String.format("account/change-work-email/%s", changePasswordToken));
    return templateEngine.process("verify_change_work_email.html", context);
  }

  public String handleEmail(final User user) {
    final String newEmailToken = UUID.randomUUID().toString();
    user.setChangeWorkEmailToken(newEmailToken);

    final String emailContent = getVerifiedEmail(newEmailToken);

    final Timestamp sendDate = Timestamp.valueOf(LocalDateTime.now());

    final Email verifyChangeWorkEmail =
        new Email(
            systemEmailAddress,
            user.getChangeWorkEmail(),
            "Verify New Work Email",
            emailContent,
            sendDate);

    saveAndScheduleEmail(verifyChangeWorkEmail);
    return newEmailToken;
  }

  public void updateEmailStatus(final List<EmailEvent> emailEvent) {
    emailEvent.forEach(
        emailEventItem -> {
          if (StringUtils.isEmpty(emailEventItem.getMessageId())
              || emailEventItem.getEvent() == EmailStatus.INVALID) {
            logger.warn("Invalid email status update request.");
            logger.warn("Message id attached with email: {}", emailEventItem.getEmail());
            return;
          }

          final Email targetEmail = emailRepository.findByMessageId(emailEventItem.getMessageId());
          if (targetEmail == null) {
            logger.error("Can not find email with message id {}", emailEventItem.getMessageId());
            return;
          }

          if (targetEmail.getStatus() != null
              && emailEventItem.getEvent().getPriority() <= targetEmail.getStatus().getPriority()) {
            return;
          }

          if ((emailEventItem.getEvent() == EmailStatus.BOUNCE
                  || emailEventItem.getEvent() == EmailStatus.DROPPED)
              && targetEmail.getUser() != null) {
            final User targetUser = userService.findByEmailWork(targetEmail.getTo());
            final String sendUserEmail =
                targetEmail.getUser().getUserContactInformation().getEmailWork();
            sendDeliveryErrorEmail(targetUser, targetEmail.getSentAt(), sendUserEmail);
          }
          targetEmail.setStatus(emailEventItem.getEvent());
          emailRepository.save(targetEmail);
        });
  }

  private void sendDeliveryErrorEmail(
      final User targetUser, final Timestamp emailSentDate, final String targetEmail) {
    final Context context = new Context();
    context.setVariable(FRONT_END_ADDRESS, frontEndAddress);

    final UserPersonalInformation targetPersonalInformation =
        targetUser.getUserPersonalInformation();

    if (targetUser.getImageUrl() != null) {
      context.setVariable("avatarUrl", awsHelper.findFullFileUrl(targetUser.getImageUrl()));
    }
    final String backgroundColor =
        AvatarUtil.getAvatarBackground(targetPersonalInformation.getFirstName());
    context.setVariable("backgroundColor", backgroundColor);
    final String avatarText =
        AvatarUtil.getAvatarShortName(
            targetPersonalInformation.getFirstName(), targetPersonalInformation.getLastName());
    context.setVariable("avatarText", avatarText);
    context.setVariable("userName", targetPersonalInformation.getName());
    context.setVariable("userEmail", targetUser.getUserContactInformation().getEmailWork());

    final LocalDateTime sentDateTime = DateUtil.toLocalDateTime(emailSentDate);
    final String sentDate =
        sentDateTime.format(DateTimeFormatter.ofPattern("MMM d").withLocale(Locale.ENGLISH));
    context.setVariable("sentDate", sentDate);
    context.setVariable("userLink", frontEndAddress + "employees/" + targetUser.getId());
    final String emailContent = templateEngine.process("delivery_error_email.html", context);

    final String subject =
        String.format(
            "Invitation Email Could Not Be Delivered - %s", targetPersonalInformation.getName());
    final Email email =
        new Email(
            systemEmailAddress, targetEmail, subject, emailContent, DateUtil.getCurrentTime());
    scheduleEmail(email);
  }
}
