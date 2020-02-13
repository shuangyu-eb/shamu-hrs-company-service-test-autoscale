package shamu.company.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.scheduling.TaskScheduler;
import org.thymeleaf.ITemplateEngine;
import shamu.company.helpers.EmailHelper;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;
import shamu.company.utils.UuidUtil;

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
  }
}
