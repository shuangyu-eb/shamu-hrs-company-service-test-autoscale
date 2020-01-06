package shamu.company.email;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.helpers.EmailHelper;
import shamu.company.user.entity.User;

@Data
@Service
public class EmailService {

  private final EmailRepository emailRepository;

  private final TaskScheduler taskScheduler;

  private final EmailHelper emailHelper;

  private final Integer emailRetryLimit;

  private final ITemplateEngine templateEngine;

  @Value("${application.systemEmailAddress}")
  private String systemEmailAddress;

  @Value("${application.frontEndAddress}")
  private String frontEndAddress;

  @Autowired
  public EmailService(final EmailRepository emailRepository, final TaskScheduler taskScheduler,
      final EmailHelper emailHelper, @Value("${email.retryLimit}") final Integer emailRetryLimit,
      final ITemplateEngine templateEngine) {
    this.emailRepository = emailRepository;
    this.taskScheduler = taskScheduler;
    this.emailHelper = emailHelper;
    this.emailRetryLimit = emailRetryLimit;
    this.templateEngine = templateEngine;
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
      final String welcomeMessage,
      final String resetPasswordToken) {
    return getWelcomeEmailContext(welcomeMessage, resetPasswordToken, "");
  }

  public Context getWelcomeEmailContext(
      String welcomeMessage,
      final String resetPasswordToken,
      final String toEmail) {
    final Context context = new Context();
    context.setVariable("frontEndAddress", frontEndAddress);
    final String emailAddress = getEncodedEmailAddress(toEmail);
    String targetLink = frontEndAddress + "account/password/" + resetPasswordToken;
    if (!"".equals(emailAddress)) {
      targetLink += "/" + emailAddress;
    }
    context.setVariable("createPasswordAddress", targetLink);
    welcomeMessage = getFilteredWelcomeMessage(welcomeMessage);
    context.setVariable("welcomeMessage", welcomeMessage);
    return context;
  }

  public Context findWelcomeEmailPreviewContext(final User currentUser,
                                                final String welcomeEmailPersonalMessage) {
    final Context context = getWelcomeEmailContext(welcomeEmailPersonalMessage,
        null);
    context.setVariable("createPasswordAddress", "#");
    context.setVariable("companyName", currentUser.getCompany().getName());
    return context;
  }

  private String getEncodedEmailAddress(final String emailAddress) {
    if (Strings.isBlank(emailAddress) || !Pattern.matches("^[a-zA-Z0-9@.+]*$", emailAddress)) {
      return "";
    }
    return emailAddress
        .replace("@", "-at-")
        .replaceAll("\\.", "-dot-");
  }

  private String getFilteredWelcomeMessage(String welcomeMessage) {
    if (Strings.isBlank(welcomeMessage)) {
      welcomeMessage = "";
    }
    return welcomeMessage
        .replaceAll("href\\s*=\\s*(['\"])\\s*(?!http[s]?).+?\\1", "#")
        .replaceAll("<script(.*)?>.*</script>", "");
  }

  public String getResetPasswordEmail(final String passwordRestToken) {
    final Context context = new Context();
    context.setVariable("frontEndAddress", frontEndAddress);
    context.setVariable(
        "passwordResetAddress", String.format("account/reset-password/%s", passwordRestToken));
    return templateEngine.process("password_reset_email.html", context);
  }

  private String getVerifiedEmail(final String changePasswordToken) {
    final Context context = new Context();
    context.setVariable("frontEndAddress", frontEndAddress);
    context.setVariable(
        "changePasswordToken", String.format("account/change-work-email/%s", changePasswordToken));
    return templateEngine.process("verify_change_work_email.html", context);
  }

  public String handleEmail(final User user) {
    final String newEmailToken = UUID.randomUUID().toString();
    user.setChangeWorkEmailToken(newEmailToken);

    final String emailContent = getVerifiedEmail(newEmailToken);

    final Timestamp sendDate = Timestamp.valueOf(LocalDateTime.now());

    final Email verifyChangeWorkEmail = new Email(systemEmailAddress,
        user.getChangeWorkEmail(), "Verify New Work Email", emailContent, sendDate);

    saveAndScheduleEmail(verifyChangeWorkEmail);
    return newEmailToken;
  }

}
