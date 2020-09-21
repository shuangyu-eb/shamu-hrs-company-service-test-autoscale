package shamu.company.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.common.entity.Tenant;
import shamu.company.common.service.TenantService;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.email.entity.Email;
import shamu.company.email.event.EmailEvent;
import shamu.company.email.event.EmailStatus;
import shamu.company.email.repository.EmailRepository;
import shamu.company.email.service.EmailService;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.scheduler.QuartzJobScheduler;
import shamu.company.user.dto.CurrentUserDto;
import shamu.company.user.dto.IndeedUserDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;
import shamu.company.utils.UuidUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class EmailServiceTests {

  @Mock private EmailRepository emailRepository;

  @Mock private ITemplateEngine templateEngine;

  @Mock private UserService userService;

  private Email email;

  private Integer emailRetryLimit;

  private EmailService emailService;

  @Mock private AwsHelper awsHelper;

  @Mock private QuartzJobScheduler quartzJobScheduler;

  @Mock private CompanyService companyService;

  @Mock private TenantService tenantService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    emailRetryLimit = 5;
    emailService =
        new EmailService(
            emailRepository,
            emailRetryLimit,
            templateEngine,
            userService,
            awsHelper,
            quartzJobScheduler,
            companyService,
            tenantService);
    email = new Email();
  }

  @Test
  void testSave() {
    emailService.save(email);
    Mockito.verify(emailRepository, Mockito.times(1)).save(email);
  }

  @Nested
  class TestRescheduleFailedEmail {

    @Test
    void whenRetryLimitOutOfRange_thenReturn() throws Exception {
      final List<Email> emails = Arrays.asList(email);
      email.setRetryCount(emailRetryLimit - 1);
      Whitebox.invokeMethod(emailService, "rescheduleFailedEmails", emails);
      Mockito.verify(emailRepository, Mockito.times(1)).saveAll(emails);
    }
  }

  @Nested
  class UpdateEmailStatus {

    private final List<EmailEvent> emailEvents = new ArrayList<>();
    private Email targetEmail;

    @BeforeEach
    void init() {
      emailEvents.clear();
      targetEmail = new Email();
      targetEmail.setMessageId(UuidUtil.getUuidString());
      targetEmail.setSentAt(DateUtil.getCurrentTime());
      Mockito.when(emailRepository.findByMessageId(Mockito.anyString())).thenReturn(targetEmail);

      final Tenant tenant = new Tenant();
      Mockito.when(tenantService.findTenantByUserEmailWork(Mockito.any())).thenReturn(tenant);
    }

    @Test
    void whenNoMessageId_thenShouldReturn() {
      emailEvents.add(new EmailEvent());
      emailService.updateEmailStatus(emailEvents);
      Mockito.verify(emailRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void whenEmailStatusIsInvalid_thenShouldReturn() {
      final EmailStatus emailStatus = EmailStatus.INVALID;
      final EmailEvent emailEvent = new EmailEvent();
      emailEvent.setEvent(emailStatus);
      emailEvents.add(emailEvent);
      emailService.updateEmailStatus(emailEvents);
      Mockito.verify(emailRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void whenCanNotFindTargetEmail_thenShouldReturn() {
      emailEvents.add(new EmailEvent());
      Mockito.when(emailRepository.findByMessageId(Mockito.anyString())).thenReturn(null);
      emailService.updateEmailStatus(emailEvents);
      Mockito.verify(emailRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void whenExistingEmailStatusPriorityGreater_thenShouldReturn() {
      final EmailEvent emailEvent = new EmailEvent();
      emailEvent.setMessageId(UuidUtil.getUuidString());
      emailEvent.setEvent(EmailStatus.PROCESSED);
      emailEvents.add(emailEvent);
      targetEmail.setStatus(EmailStatus.DELIVERED);
      emailService.updateEmailStatus(emailEvents);
      Mockito.verify(emailRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void whenAllOk_thenShouldSuccess() {
      final User targetUser = new User();
      targetUser.setUserPersonalInformation(new UserPersonalInformation());
      targetUser.getUserPersonalInformation().setFirstName("Example");
      targetUser.getUserPersonalInformation().setLastName("A");
      targetUser.setUserContactInformation(new UserContactInformation());
      targetUser.getUserContactInformation().setEmailWork("example@example.com");
      targetEmail.setUser(targetUser);
      targetEmail.setStatus(EmailStatus.PROCESSED);

      final EmailEvent emailEvent = new EmailEvent();
      emailEvent.setMessageId(UuidUtil.getUuidString());
      emailEvent.setEvent(EmailStatus.DELIVERED);
      emailEvents.add(emailEvent);
      emailService.updateEmailStatus(emailEvents);
      Mockito.verify(emailRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void whenEmailEventIsBounce_thenShouldSuccess() {
      final EmailEvent emailEvent = new EmailEvent();
      final User targetUser = new User();
      targetUser.setId("id");
      final UserContactInformation userContactInformation = new UserContactInformation();
      userContactInformation.setEmailWork("128281928@gmail.com");
      final UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
      userPersonalInformation.setFirstName("firstName");
      userPersonalInformation.setLastName("lastName");
      targetUser.setUserPersonalInformation(userPersonalInformation);
      targetUser.setUserContactInformation(userContactInformation);
      targetEmail.setUser(targetUser);
      emailEvent.setMessageId(UuidUtil.getUuidString());
      emailEvent.setEvent(EmailStatus.BOUNCE);
      emailEvents.add(emailEvent);
      targetEmail.setStatus(EmailStatus.PROCESSED);
      Mockito.when(userService.findByEmailWork(Mockito.any())).thenReturn(targetUser);
      emailService.updateEmailStatus(emailEvents);
      Mockito.verify(userService, Mockito.times(1)).findByEmailWork(Mockito.any());
    }
  }

  @Nested
  class saveAndScheduleEmail {
    Email email;

    @BeforeEach
    void init() {
      email = new Email();
    }

    @Test
    void whenSaveAndScheduleEmail_thenShouldSuccess() {
      Mockito.when(emailRepository.save(Mockito.any())).thenReturn(new Email());
      emailService.saveAndScheduleEmail(email);
      Mockito.verify(emailRepository, Mockito.times(1)).save(Mockito.any());
    }
  }

  @Nested
  class getWelcomeEmail {
    Context context;

    @BeforeEach
    void init() {
      context = new Context();
    }

    @Test
    void whenGetWelcomeEmail_thenShouldSuccess() {
      emailService.getWelcomeEmail(context);
    }
  }

  @Nested
  class findWelcomeEmailPreviewContext {
    User currentUser;
    String welcomeEmailPersonalMessage;

    @BeforeEach
    void init() {
      currentUser = new User();
      welcomeEmailPersonalMessage = "a";
      final Company company = new Company();
      company.setName("companyName");
      Mockito.when(companyService.getCompany()).thenReturn(company);
    }

    @Test
    void whenFindWelcomeEmailPreviewContext_thenShouldSuccess() {
      final Context context =
          emailService.findWelcomeEmailPreviewContext(welcomeEmailPersonalMessage);
      assertThat(context).isNotNull();
    }
  }

  @Nested
  class getResetPasswordEmail {
    String passwordRestToken;
    String toEmail;

    @BeforeEach
    void init() {
      passwordRestToken = "token";
    }

    @Test
    void whenGetResetPasswordEmail_thenShouldSuccess() {
      final String result = emailService.getResetPasswordEmail(passwordRestToken, toEmail);
      assertThat(result).isNull();
    }
  }

  @Nested
  class handleEmail {
    User user;

    @BeforeEach
    void init() {
      user = new User();
    }

    @Test
    void whenHandleEmail_thenShouldSuccess() {
      final UserPersonalInformation information = new UserPersonalInformation();
      information.setFirstName("firstName");
      information.setLastName("lastName");
      user.setUserPersonalInformation(information);
      Mockito.when(emailRepository.save(Mockito.any())).thenReturn(new Email());
      final String token = emailService.handleEmail(user);
      assertThat(token).isNotNull();
    }
  }

  @Nested
  class sendDeliveryErrorEmail {
    User user;
    Timestamp emailSentDate;
    String targetEmail;

    @BeforeEach
    void init() {
      user = new User();
      emailSentDate = new Timestamp(2132666636);
      targetEmail = "email";
    }

    @Test
    void whenSendDeliveryErrorEmail_thenShouldSuccess() throws Exception {
      final UserPersonalInformation targetPersonalInformation = new UserPersonalInformation();
      targetPersonalInformation.setFirstName("firstName");
      targetPersonalInformation.setLastName("lastName");
      final UserContactInformation userContactInformation = new UserContactInformation();
      userContactInformation.setEmailWork("emailWork");
      user.setUserContactInformation(userContactInformation);
      user.setUserPersonalInformation(targetPersonalInformation);
      Whitebox.invokeMethod(
          emailService, "sendDeliveryErrorEmail", user, emailSentDate, targetEmail);
    }
  }

  @Test
  void sendEmailToOtherAdminsWhenNewOneAdded() throws Exception {
    final String companyId = "1";
    final String promotedEmployeeId = "2";
    final String currentUserId = "3";

    final User promotedEmployee = new User();
    promotedEmployee.setId(promotedEmployeeId);
    final UserPersonalInformation promotedEmployeePersonalInformation =
        new UserPersonalInformation();
    promotedEmployeePersonalInformation.setFirstName("promoted");
    promotedEmployeePersonalInformation.setLastName("employee");
    promotedEmployee.setUserPersonalInformation(promotedEmployeePersonalInformation);
    Mockito.when(userService.findById(promotedEmployeeId)).thenReturn(promotedEmployee);

    final CurrentUserDto currentUserDto = new CurrentUserDto();
    currentUserDto.setName("current user");
    Mockito.when(userService.getCurrentUserInfo(currentUserId)).thenReturn(currentUserDto);

    final List<User> admins = new ArrayList<>();
    final User admin = new User();
    final UserContactInformation adminContactInfo = new UserContactInformation();
    final UserPersonalInformation adminPersonalInfo = new UserPersonalInformation();
    adminContactInfo.setEmailWork("mock-admin@mock.com");
    adminPersonalInfo.setFirstName("first");
    adminPersonalInfo.setLastName("last");
    admin.setUserContactInformation(adminContactInfo);
    admin.setUserPersonalInformation(adminPersonalInfo);
    admins.add(admin);

    final List<User> superAdmins = new ArrayList<>();
    final User superAdmin = new User();
    final UserContactInformation superAdminContactInfo = new UserContactInformation();
    final UserPersonalInformation superAdminPersonalInfo = new UserPersonalInformation();
    superAdminContactInfo.setEmailWork("mock-super-admin@mock.com");
    superAdminPersonalInfo.setFirstName("first");
    superAdminPersonalInfo.setLastName("last");
    superAdmin.setUserContactInformation(superAdminContactInfo);
    superAdmin.setUserPersonalInformation(superAdminPersonalInfo);
    superAdmins.add(superAdmin);

    Mockito.when(userService.findUsersByCompanyIdAndUserRole(Role.ADMIN.getValue()))
        .thenReturn(admins);
    Mockito.when(userService.findUsersByCompanyIdAndUserRole(Role.SUPER_ADMIN.getValue()))
        .thenReturn(superAdmins);
    Whitebox.invokeMethod(
        emailService, "sendEmailToOtherAdminsWhenNewOneAdded", promotedEmployeeId, currentUserId);
  }

  @Nested
  class findFirstByToAndSubjectOrderBySendDateDesc {
    String email;
    String s;

    @BeforeEach
    void init() {
      email = "email";
      s = "s";
    }

    @Test
    void whenFindFirstByToAndSubjectOrderBySendDateDesc_thenShouldSuccess() {
      emailService.findFirstByToAndSubjectOrderBySendDateDesc(email, s);
      Mockito.verify(emailRepository, Mockito.times(1))
          .findFirstByToAndSubjectOrderBySendDateDesc(email, s);
    }
  }

  @Nested
  class getEncodedEmailAddress {
    String email;

    @Test
    void whenGetEncodedEmailAddressWithWrongFormat_thenShouldGetBlank() {
      email = "123$.com";
      final String result = emailService.getEncodedEmailAddress(email);
      assertThat(result).isEqualTo("");
    }

    @Test
    void whenGetEncodedEmailAddressWithRightFormat_thenShouldGetDecodedEmailAddress() {
      email = "123@example.com";
      final String result = emailService.getEncodedEmailAddress(email);
      assertThat(result).isNotEqualTo("");
    }
  }

  @Nested
  class AttendanceEmail {
    String periodId = "test_period_id";
    Timestamp sendDate = new Timestamp(new Date().getTime());

    @Test
    void caseSubmit_shouldSucceed() {
      assertThatCode(
              () ->
                  emailService.getAttendanceNotificationEmails(
                      periodId, EmailService.EmailNotification.SUBMIT_TIME_SHEET, sendDate))
          .doesNotThrowAnyException();
    }

    @Test
    void casePayRoll_shouldSucceed() {
      assertThatCode(
              () ->
                  emailService.getAttendanceNotificationEmails(
                      periodId, EmailService.EmailNotification.RUN_PAYROLL, sendDate))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  class sendVerificationEmail {

    @Test
    void whenCalled_thenShouldSendEmail() {
      Email savedEmail = new Email();
      savedEmail.setId(UuidUtil.getUuidString());
      savedEmail.setSendDate(Timestamp.valueOf(LocalDateTime.now()));
      Mockito.when(emailService.save(Mockito.any())).thenReturn(savedEmail);

      emailService.sendVerificationEmail(Mockito.anyString(), Mockito.anyString());
      Mockito.verify(emailRepository, Mockito.times(1))
              .save(Mockito.any(Email.class));
      Mockito.verify(quartzJobScheduler, Mockito.times(1))
              .addOrUpdateJobSchedule(Mockito.any(), Mockito.anyString(),Mockito.anyString(), Mockito.anyMap(), Mockito.any());
    }
  }
}
