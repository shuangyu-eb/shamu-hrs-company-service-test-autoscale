package shamu.company.email;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.email.entity.Email;
import shamu.company.email.service.EmailService;
import shamu.company.email.worker.EmailWorker;

public class EmailWorkerTests {
  @Mock EmailService emailService;

  private EmailWorker emailWorker;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    emailWorker = new EmailWorker(emailService);
  }

  @Test
  void whenAfterPropertiesSet_thenShouldSuccess() {
    List<Email> emailTaskList = new ArrayList<>();
    Email email = new Email();
    emailTaskList.add(email);
    Mockito.when(emailService.findAllUnfinishedTasks()).thenReturn(emailTaskList);
    emailWorker.afterPropertiesSet();
    Mockito.verify(emailService, Mockito.times(1)).findAllUnfinishedTasks();
  }
}
