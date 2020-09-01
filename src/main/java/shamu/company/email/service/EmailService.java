package shamu.company.email.service;

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
import shamu.company.scheduler.QuartzJobScheduler;
import shamu.company.scheduler.job.SendEmailJob;
import shamu.company.scheduler.job.SendEmailsJob;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserService;
import shamu.company.utils.AvatarUtil;
import shamu.company.utils.DateUtil;
import shamu.company.utils.HtmlUtils;
import shamu.company.utils.UuidUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EmailService {
  private static final String FRONT_END_ADDRESS = "frontEndAddress";
  private static final String SUBJECT_TEXT = "subjectText";
  private static final String CONTENT_TEXT = "contentText";
  private static final String BUTTON_TEXT = "buttonText";

  public enum EmailNotification {
    SUBMIT_TIME_SHEET(
        "You have hours pending approval",
        "Your company attendance hours need to be approved and submitted within the next 8 hours.",
        "Review Attendance Hours"),
    RUN_PAYROLL(
        "Hours Pending Approval",
        "There are attendance hours pending your approval that are due in 8 hours.",
        "Approve Hours"),
    RUN_PAYROLL_TIME_OUT(
        "Hours Past Due",
        "There are attendance hours pending your approval that are past due.",
        "Approve Hours");
    private final String subjectContext;
    private final String contentContext;
    private final String buttonContext;

    EmailNotification(
        final String subjectContext, final String contentContext, final String buttonContext) {
      this.subjectContext = subjectContext;
      this.contentContext = contentContext;
      this.buttonContext = buttonContext;
    }

    public String getSubjectContext() {
      return subjectContext;
    }

    public String getContentContext() {
      return contentContext;
    }

    public String getButtonText() {
      return buttonContext;
    }
  }

  private static final String NEW_ADMIN_ADDED_TO_HRIS = "New Admin Added to HRIS";

  private final EmailRepository emailRepository;

  private final Integer emailRetryLimit;

  private final ITemplateEngine templateEngine;
  private final UserService userService;
  private final AwsHelper awsHelper;
  private final Logger logger = LoggerFactory.getLogger(EmailService.class);

  @Value("${application.systemEmailAddress}")
  private String systemEmailAddress;

  @Value("${application.frontEndAddress}")
  private String frontEndAddress;

  @Value("${application.systemEmailFirstName}")
  private String systemEmailFirstName;

  @Value("${application.systemEmailLastName}")
  private String systemEmailLastName;

  private final QuartzJobScheduler quartzJobScheduler;

  private static final String CURRENT_YEAR = "currentYear";

  private static final String AMERICA_MANAGUA = "America/Managua";

  @Autowired
  public EmailService(
      final EmailRepository emailRepository,
      final TaskScheduler taskScheduler,
      final EmailHelper emailHelper,
      @Value("${email.retryLimit}") final Integer emailRetryLimit,
      final ITemplateEngine templateEngine,
      @Lazy final UserService userService,
      final AwsHelper awsHelper,
      final QuartzJobScheduler quartzJobScheduler) {
    this.emailRepository = emailRepository;
    this.emailRetryLimit = emailRetryLimit;
    this.templateEngine = templateEngine;
    this.userService = userService;
    this.awsHelper = awsHelper;
    this.quartzJobScheduler = quartzJobScheduler;
  }

  public Email save(final Email email) {
    return emailRepository.save(email);
  }

  public List<Email> saveAll(final List<Email> emails) {
    return emailRepository.saveAll(emails);
  }

  public List<Email> findAllUnfinishedTasks() {
    return emailRepository.findAllUnfinishedTasks(emailRetryLimit);
  }

  public void scheduleEmail(final Email email) {
    final Timestamp sendDate = email.getSendDate();
    final String messageId = UuidUtil.getUuidString();
    email.setMessageId(messageId);
    final Map<String, Object> jobParameter = new HashMap<>();
    jobParameter.put("email", email);
    quartzJobScheduler.addOrUpdateJobSchedule(
        SendEmailJob.class,
        messageId,
        "sendEmail",
        jobParameter,
        sendDate == null ? Timestamp.valueOf(LocalDateTime.now()) : sendDate);
  }

  public void saveAndScheduleEmail(final Email email) {
    save(email);
    scheduleEmail(email);
  }

  public void saveAndScheduleSendEmails(final List<Email> emails) {
    saveAll(emails);
    scheduleSendEmails(emails);
  }

  private void scheduleSendEmails(final List<Email> emails) {
    if (emails.size() == 0) {
      return;
    }
    final Timestamp sendDate = emails.get(0).getSendDate();
    final List<String> messageIdList = new ArrayList<>();
    emails.forEach(email -> messageIdList.add(email.getMessageId()));
    final Map<String, Object> jobParameter = new HashMap<>();
    jobParameter.put("messageIdList", messageIdList);
    quartzJobScheduler.addOrUpdateJobSchedule(
        SendEmailsJob.class,
        "",
        "sendEmails",
        jobParameter,
        sendDate == null ? Timestamp.valueOf(LocalDateTime.now()) : sendDate);
  }

  private List<Email> getFromSystemEmails(
      final List<User> users,
      final String subject,
      final String content,
      final Timestamp sendDate) {
    return users.stream()
        .map(
            user -> {
              Email email =
                  new Email(
                      systemEmailAddress,
                      systemEmailFirstName + "-" + systemEmailLastName,
                      user.getUserContactInformation().getEmailWork(),
                      user.getUserPersonalInformation().getName(),
                      subject);
              email.setContent(content);
              email.setSendDate(sendDate);
              email.setMessageId(UuidUtil.getUuidString());
              return email;
            })
        .collect(Collectors.toList());
  }

  public void rescheduleFailedEmails(final List<Email> emails) {
    if (emails.isEmpty()) {
      return;
    }
    final Email email = emails.get(0);
    final Integer currentRetryCount = getRetryCount(email);

    if (currentRetryCount > emailRetryLimit) {
      return;
    }

    final LocalDateTime afterOneHour = LocalDateTime.now().plusHours(1);
    emails.forEach(
        email1 -> {
          email.setRetryCount(currentRetryCount);
          email.setSendDate(Timestamp.valueOf(afterOneHour));
        });
    saveAndScheduleSendEmails(emails);
  }

  private int getRetryCount(final Email email) {
    return email.getRetryCount() == null ? 1 : email.getRetryCount() + 1;
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
    final byte[] reverseEmails = StringUtils.reverse(emailAddress).getBytes();
    return Base64.getEncoder().encodeToString(reverseEmails);
  }

  private String getFilteredWelcomeMessage(String welcomeMessage) {
    if (Strings.isBlank(welcomeMessage)) {
      welcomeMessage = "";
    }
    return HtmlUtils.filterWelcomeMessage(welcomeMessage);
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
    final ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("UTC"));
    final String currentYear =
        DateUtil.formatDateTo(
            zonedDateTime.withZoneSameInstant(ZoneId.of(AMERICA_MANAGUA)).toLocalDateTime(),
            "YYYY");
    context.setVariable(CURRENT_YEAR, currentYear);
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
            systemEmailFirstName + "-" + systemEmailLastName,
            user.getChangeWorkEmail(),
            user.getUserPersonalInformation().getName(),
            "Verify New Work Email");

    verifyChangeWorkEmail.setContent(emailContent);
    verifyChangeWorkEmail.setSendDate(sendDate);
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
    final ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("UTC"));
    final String currentYear =
        DateUtil.formatDateTo(
            zonedDateTime.withZoneSameInstant(ZoneId.of(AMERICA_MANAGUA)).toLocalDateTime(),
            "YYYY");
    context.setVariable(CURRENT_YEAR, currentYear);
    final String emailContent = templateEngine.process("delivery_error_email.html", context);

    final String subject =
        String.format(
            "Invitation Email Could Not Be Delivered - %s", targetPersonalInformation.getName());
    final Email email =
        new Email(
            systemEmailAddress,
            systemEmailFirstName + "-" + systemEmailLastName,
            targetEmail,
            targetUser.getUserPersonalInformation().getName(),
            subject);
    email.setContent(emailContent);
    email.setSendDate(DateUtil.getCurrentTime());
    scheduleEmail(email);
  }

  public List<Email> getAttendanceNotificationEmails(
      final String periodId, final EmailNotification emailNotification, final Timestamp sendDate) {
    List<User> users = new ArrayList<>();
    switch (emailNotification) {
      case SUBMIT_TIME_SHEET:
        users = userService.listNotSubmitTimeSheetsUsers(periodId);
        break;
      case RUN_PAYROLL:
      case RUN_PAYROLL_TIME_OUT:
        users = userService.listHasPendingTimeSheetsManagerAndAdmin(periodId);
        break;
      default:
        break;
    }

    return getFromSystemEmails(
        users,
        emailNotification.getSubjectContext(),
        getAttendanceNotificationEmailContent(emailNotification),
        sendDate);
  }

  private String getAttendanceNotificationEmailContent(final EmailNotification emailNotification) {
    final Context context = new Context();
    context.setVariable(FRONT_END_ADDRESS, frontEndAddress);
    context.setVariable(SUBJECT_TEXT, emailNotification.getSubjectContext());
    context.setVariable(CONTENT_TEXT, emailNotification.getContentContext());
    context.setVariable(BUTTON_TEXT, emailNotification.getButtonText());
    return templateEngine.process("attendance_notification.html", context);
  }

  public void sendEmailToOtherAdminsWhenNewOneAdded(
      final String promotedEmployeeId, final String currentUserId, final String companyId) {
    final User promotedEmployee = userService.findById(promotedEmployeeId);
    final String promotedEmployeeName = promotedEmployee.getUserPersonalInformation().getName();
    final String currentUserName = userService.getCurrentUserInfo(currentUserId).getName();

    final Context context = new Context();
    context.setVariable(FRONT_END_ADDRESS, frontEndAddress);
    context.setVariable("promotedEmployeeName", promotedEmployeeName);
    context.setVariable("promoterName", currentUserName);
    context.setVariable("promotedEmployeeId", promotedEmployeeId);
    final ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("UTC"));
    final String currentYear =
        DateUtil.formatDateTo(
            zonedDateTime.withZoneSameInstant(ZoneId.of(AMERICA_MANAGUA)).toLocalDateTime(),
            "YYYY");
    context.setVariable(CURRENT_YEAR, currentYear);
    final String emailContent = templateEngine.process("add_new_admin_email.html", context);
    final List<User> admins =
        userService.findUsersByCompanyIdAndUserRole(companyId, Role.ADMIN.getValue());
    final List<User> superAdmins =
        userService.findUsersByCompanyIdAndUserRole(companyId, Role.SUPER_ADMIN.getValue());
    admins.addAll(superAdmins);
    admins.remove(promotedEmployee);
    final String fromName = systemEmailFirstName + "-" + systemEmailLastName;

    admins.forEach(
        admin -> {
          final String adminEmailWork = admin.getUserContactInformation().getEmailWork();
          final Email email =
              new Email(
                  systemEmailAddress,
                  fromName,
                  adminEmailWork,
                  admin.getUserPersonalInformation().getName(),
                  NEW_ADMIN_ADDED_TO_HRIS);
          email.setContent(emailContent);
          email.setSendDate(Timestamp.valueOf(LocalDateTime.now()));
          scheduleEmail(email);
        });
  }

  public List<Email> listByMessageIds(final List<String> messageIds) {
    return emailRepository.findByMessageIdIn(messageIds);
  }
}
