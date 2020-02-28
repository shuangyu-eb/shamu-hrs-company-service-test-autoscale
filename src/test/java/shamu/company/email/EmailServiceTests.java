package shamu.company.email;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.scheduling.TaskScheduler;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import shamu.company.company.entity.Company;
import shamu.company.email.entity.Email;
import shamu.company.email.event.EmailEvent;
import shamu.company.email.event.EmailStatus;
import shamu.company.email.repository.EmailRepository;
import shamu.company.email.service.EmailService;
import shamu.company.helpers.EmailHelper;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;
import shamu.company.utils.UuidUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

class EmailServiceTests {

  @Mock private EmailRepository emailRepository;

  @Mock private TaskScheduler taskScheduler;

  @Mock private EmailHelper emailHelper;

  @Mock private ITemplateEngine templateEngine;

  @Mock private UserService userService;

  private Email email;

  private Integer emailRetryLimit;

  private EmailService emailService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    emailRetryLimit = 5;
    emailService =
        new EmailService(
            emailRepository,
            taskScheduler,
            emailHelper,
            emailRetryLimit,
            templateEngine,
            userService);
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
      email.setRetryCount(emailRetryLimit - 1);
      Whitebox.invokeMethod(emailService, "rescheduleFailedEmail", email);
      Mockito.verify(emailRepository, Mockito.times(1)).save(email);
    }

    @Test
    void whenRetryLimitIntheRange_thenContinue() throws Exception {
      email.setRetryCount(emailRetryLimit - 2);
      Whitebox.invokeMethod(emailService, "rescheduleFailedEmail", email);
      Mockito.verify(emailRepository, Mockito.times(2)).save(email);
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
    }

    @Test
    void whenNoMessageId_thenShouldReturn() {
      emailEvents.add(new EmailEvent());
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
      UserContactInformation userContactInformation = new UserContactInformation();
      userContactInformation.setEmailWork("128281928@gmail.com");
      UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
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
      Mockito.verify(userService, Mockito.times(1))
          .findByEmailWork(Mockito.any());
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
      currentUser.setCompany(company);
    }
    @Test
    void whenFindWelcomeEmailPreviewContext_thenShouldSuccess() {
      Context context = emailService
          .findWelcomeEmailPreviewContext(currentUser, welcomeEmailPersonalMessage);
      Assertions.assertNotNull(context);
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
      String result = emailService.getResetPasswordEmail(passwordRestToken,toEmail);
      Assertions.assertEquals(null, result);
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
      String token = emailService.handleEmail(user);
      Assertions.assertNotNull(token);
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
      UserPersonalInformation targetPersonalInformation = new UserPersonalInformation();
      targetPersonalInformation.setFirstName("firstName");
      targetPersonalInformation.setLastName("lastName");
      UserContactInformation userContactInformation = new UserContactInformation();
      userContactInformation.setEmailWork("emailWork");
      user.setUserContactInformation(userContactInformation);
      user.setUserPersonalInformation(targetPersonalInformation);
      Whitebox.invokeMethod(
          emailService, "sendDeliveryErrorEmail", user, emailSentDate, targetEmail);
    }
  }

  @Nested
  class getEmailTask {
    Email email;
    @BeforeEach
    void init() {
      email = new Email();
    }
    @Test
    void whenGetEmailTask_thenShouldSuccess() {
      emailService.getEmailTask(email);
      Mockito.verify(emailRepository, Mockito.times(0)).save(Mockito.any());
    }
  }

  @Nested
  class findAllUnfinishedTasks {
    @Test
    void whenFindAllUnfinishedTasks_thenShouldSuccess() {
      emailService.findAllUnfinishedTasks();
      Mockito.verify(emailRepository,Mockito.times(1))
          .findAllUnfinishedTasks(Mockito.any());
    }
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
      emailService.findFirstByToAndSubjectOrderBySendDateDesc(email,s);
      Mockito.verify(emailRepository, Mockito.times(1))
          .findFirstByToAndSubjectOrderBySendDateDesc(email,s);
    }
  }
}
