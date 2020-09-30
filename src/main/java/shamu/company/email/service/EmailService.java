package shamu.company.email.service;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.common.entity.Tenant;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.common.service.TenantService;
import shamu.company.company.service.CompanyService;
import shamu.company.email.entity.Email;
import shamu.company.email.event.EmailEvent;
import shamu.company.email.event.EmailStatus;
import shamu.company.email.repository.EmailRepository;
import shamu.company.helpers.EmailHelper;
import shamu.company.helpers.auth0.Auth0Helper;
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
import java.util.Calendar;
import java.util.Date;
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
  private static final String IS_INDEED_ENV = "isIndeedENV";

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

  public enum EmailTemplate {
    MY_HOUR_EDITED("Your Hours Have Been Edited", "my_hour_edited_email");
    private final String subject;
    private final String templateName;

    EmailTemplate(final String subject, final String templateName) {
      this.subject = subject;
      this.templateName = templateName + ".html";
    }

    public String getSubject() {
      return subject;
    }

    public String getTemplateName() {
      return templateName;
    }
  }

  private static final String NEW_ADMIN_ADDED_TO_HRIS = "New Admin Added to HRIS";

  private final EmailRepository emailRepository;

  private final CompanyService companyService;

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

  private final TenantService tenantService;

  private static final String CURRENT_YEAR = "currentYear";

  private static final String AMERICA_MANAGUA = "America/Managua";

  private static final String COMPANY_ID = "companyId";

  private final Auth0Helper auth0Helper;

  private final EmailHelper emailHelper;

  @Autowired
  public EmailService(
      final EmailRepository emailRepository,
      @Value("${email.retryLimit}") final Integer emailRetryLimit,
      final ITemplateEngine templateEngine,
      @Lazy final UserService userService,
      final AwsHelper awsHelper,
      final QuartzJobScheduler quartzJobScheduler,
      final CompanyService companyService,
      final TenantService tenantService,
      final Auth0Helper auth0Helper,
      final EmailHelper emailHelper) {
    this.emailRepository = emailRepository;
    this.emailRetryLimit = emailRetryLimit;
    this.templateEngine = templateEngine;
    this.userService = userService;
    this.awsHelper = awsHelper;
    this.quartzJobScheduler = quartzJobScheduler;
    this.companyService = companyService;
    this.tenantService = tenantService;
    this.auth0Helper = auth0Helper;
    this.emailHelper = emailHelper;
  }

  public Email save(final Email email) {
    return emailRepository.save(email);
  }

  public List<Email> saveAll(final List<Email> emails) {
    return emailRepository.saveAll(emails);
  }

  public Email get(final String emailId) {
    return emailRepository
        .findById(emailId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Email with id %s not found!", emailId), emailId, "email"));
  }

  private void scheduleEmail(final Email email) {
    Timestamp sendDate = email.getSendDate();
    if (sendDate == null) {
      sendDate = Timestamp.valueOf(LocalDateTime.now());
    }
    final String messageId = UuidUtil.getUuidString();
    email.setMessageId(messageId);
    final Map<String, Object> jobParameter = new HashMap<>();
    jobParameter.put("emailId", email.getId());
    jobParameter.put(COMPANY_ID, TenantContext.getCurrentTenant());
    quartzJobScheduler.addOrUpdateJobSchedule(
        SendEmailJob.class,
        email.getId(),
        "sendEmail",
        jobParameter,
        sendDate == null ? Timestamp.valueOf(LocalDateTime.now()) : sendDate);
  }

  public Email sendEmail(
      final User fromUser,
      final User toUser,
      final EmailTemplate emailTemplate,
      final Map<String, Object> templateVariables,
      final Timestamp sendDate) {
    if (!templateVariables.containsKey(FRONT_END_ADDRESS)) {
      templateVariables.put(FRONT_END_ADDRESS, frontEndAddress);
    }
    final String emailContent =
        templateEngine.process(
            emailTemplate.getTemplateName(), new Context(Locale.ENGLISH, templateVariables));

    final Email email =
        Email.builder()
            .from(systemEmailAddress)
            .fromName(fromUser.getUserPersonalInformation().getName())
            .to(toUser.getUserContactInformation().getEmailWork())
            .toName(toUser.getUserPersonalInformation().getName())
            .subject(emailTemplate.getSubject())
            .content(emailContent)
            .sendDate(sendDate)
            .build();
    saveAndScheduleEmail(email);

    return email;
  }

  public Email saveAndScheduleEmail(Email email) {
    email.setMessageId(UuidUtil.getUuidString());
    email = save(email);
    scheduleEmail(email);
    return email;
  }

  // If send to emails like a+1@gmail.com and a+2@gmail.com...
  // only one email will be accepted
  public List<Email> saveAndScheduleEmails(List<Email> emails) {
    emails.forEach(email -> email.setMessageId(UuidUtil.getUuidString()));
    emails = saveAll(emails);
    scheduleSendEmails(emails);
    return emails;
  }

  // right now send
  public Email saveAndSendEmail(Email email) {
    email.setMessageId(UuidUtil.getUuidString());
    email.setSendDate(new Timestamp(new Date().getTime()));
    email = save(email);
    emailHelper.send(email);
    return email;
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
    jobParameter.put(COMPANY_ID, TenantContext.getCurrentTenant());
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
    saveAndScheduleEmails(emails);
  }

  private int getRetryCount(final Email email) {
    return email.getRetryCount() == null ? 1 : email.getRetryCount() + 1;
  }

  public Email findFirstByToAndSubjectOrderBySendDateDesc(final String email, final String s) {
    return emailRepository.findFirstByToAndSubjectOrderBySendDateDesc(email, s);
  }

  public String getWelcomeEmail(final Context context) {
    context.setVariable(IS_INDEED_ENV, auth0Helper.isIndeedEnvironment());
    return templateEngine.process("employee_invitation_email.html", context);
  }

  public Context getWelcomeEmailContext(
      final String welcomeMessage, final String resetPasswordToken, final String invitationToken) {
    return getWelcomeEmailContextToEmail(
        welcomeMessage, resetPasswordToken, invitationToken, "", "", "");
  }

  public Context getWelcomeEmailContextToEmail(
      String welcomeMessage,
      final String resetPasswordToken,
      final String invitationToken,
      final String userId,
      final String toEmail,
      final String userSecret) {
    final Context context = new Context();
    context.setVariable(FRONT_END_ADDRESS, frontEndAddress);
    final String emailAddress = getEncodedEmailAddress(toEmail);
    final String companyId = TenantContext.getCurrentTenant();
    String targetLink =
        frontEndAddress + "account/password/" + resetPasswordToken + "/" + invitationToken;
    if (!"".equals(emailAddress)) {
      targetLink += "/" + emailAddress;
    }

    targetLink += "/" + getEncodedCompanyId();

    if (auth0Helper.isIndeedEnvironment()) {
      targetLink =
          frontEndAddress
              + "parse?employeeId="
              + userId
              + "&companyId="
              + companyId
              + "&userSecret="
              + userSecret;
    }
    context.setVariable("targetLinkAddress", targetLink);
    welcomeMessage = getFilteredWelcomeMessage(welcomeMessage);
    context.setVariable("welcomeMessage", welcomeMessage);
    return context;
  }

  public Context findWelcomeEmailPreviewContext(final String welcomeEmailPersonalMessage) {
    final Context context = getWelcomeEmailContext(welcomeEmailPersonalMessage, null, null);
    context.setVariable("targetLinkAddress", "#");
    context.setVariable("companyName", companyService.getCompany().getName());
    return context;
  }

  private String getEncodedCompanyId() {
    final String companyId = TenantContext.getCurrentTenant();
    if (StringUtils.isEmpty(companyId)) {
      return "";
    }
    final byte[] reverseCompanyId = StringUtils.reverse(companyId).getBytes();
    return Base64.getEncoder().encodeToString(reverseCompanyId);
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
    context.setVariable(IS_INDEED_ENV, auth0Helper.isIndeedEnvironment());
    context.setVariable(COMPANY_ID, getEncodedCompanyId());
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
    context.setVariable(COMPANY_ID, getEncodedCompanyId());
    context.setVariable(IS_INDEED_ENV, auth0Helper.isIndeedEnvironment());
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
          final Tenant tenant = tenantService.findTenantByUserEmailWork(emailEventItem.getEmail());
          handleEmailStatus(emailEventItem, tenant);
        });
  }

  private void handleEmailStatus(final EmailEvent emailEventItem, final Tenant tenant) {
    TenantContext.withInTenant(
        tenant.getCompanyId(),
        () -> {
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
    context.setVariable(IS_INDEED_ENV, auth0Helper.isIndeedEnvironment());
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
        users = userService.listMessageOnNotSubmitTimeSheetUsers(periodId);
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
    context.setVariable(IS_INDEED_ENV, auth0Helper.isIndeedEnvironment());
    return templateEngine.process("attendance_notification.html", context);
  }

  public void sendEmailToOtherAdminsWhenNewOneAdded(
      final String promotedEmployeeId, final String currentUserId) {
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
    context.setVariable(IS_INDEED_ENV, auth0Helper.isIndeedEnvironment());
    final String emailContent = templateEngine.process("add_new_admin_email.html", context);
    final List<User> admins = userService.findUsersByCompanyIdAndUserRole(Role.ADMIN.getValue());
    final List<User> superAdmins =
        userService.findUsersByCompanyIdAndUserRole(Role.SUPER_ADMIN.getValue());
    admins.addAll(superAdmins);
    admins.remove(promotedEmployee);
    final String fromName = systemEmailFirstName + "-" + systemEmailLastName;

    final List<Email> emailList = new ArrayList<>();
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
          emailList.add(email);
        });
    saveAndScheduleEmails(emailList);
  }

  public List<Email> listByMessageIds(final List<String> messageIds) {
    return emailRepository.findByMessageIdIn(messageIds);
  }

  public void sendVerificationEmail(final String email, final String userId) {
    final Context context = new Context();
    context.setVariable(FRONT_END_ADDRESS, frontEndAddress);
    context.setVariable(IS_INDEED_ENV, auth0Helper.isIndeedEnvironment());
    final String verificationUrl =
        frontEndAddress + "parse?userId=" + userId + "&emailVerified=true";
    context.setVariable("accountVerifyAddress", verificationUrl);
    final String emailContent = templateEngine.process("account_verify_email.html", context);
    final Timestamp sendDate = Timestamp.valueOf(LocalDateTime.now());
    final Email verifyEmail =
        new Email(systemEmailAddress, email, "Please Verify Your Email", emailContent, sendDate);
    saveAndScheduleEmail(verifyEmail);
  }

  public Map<String, Object> getMyHourEditedEmailParameters(
      final User manager,
      final Date date,
      final String timezone,
      final List<EmployeeTimeLog> originalTimeLogs,
      final List<EmployeeTimeLog> editedTimeLogs) {

    final Map<String, Object> parameters = new HashMap<>();

    final Calendar calendarDate = DateUtil.getCalendarInstance(date.getTime(), timezone);

    String operationDesc = "hours have been edited.";
    if (originalTimeLogs.size() == 0) {
      operationDesc = "hours have been added.";
    } else if (editedTimeLogs.size() == 0) {
      operationDesc = "hours have been deleted.";
    }

    parameters.put("managerName", manager.getUserPersonalInformation().getName());
    parameters.put(
        "date",
        DateUtil.formatCalendar(calendarDate, DateUtil.DAY_OF_WEEK_SIMPLE_MONTH_DAY_YEAR)
            + " "
            + operationDesc);
    return parameters;
  }
}
