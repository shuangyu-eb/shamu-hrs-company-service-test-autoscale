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

class EmailServiceTests {

  @Mock
  private EmailRepository emailRepository;

  @Mock
  private TaskScheduler taskScheduler;

  @Mock
  private EmailHelper emailHelper;

  @Mock
  private ITemplateEngine templateEngine;

  private Email email;

  private Integer emailRetryLimit;

  private EmailService emailService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    emailRetryLimit = 5;
    emailService = new EmailService(
        emailRepository, taskScheduler, emailHelper, emailRetryLimit, templateEngine);
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

}
