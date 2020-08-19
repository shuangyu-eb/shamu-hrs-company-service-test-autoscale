package shamu.company.scheduler;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import shamu.company.email.entity.Email;
import shamu.company.email.service.EmailService;
import shamu.company.helpers.EmailHelper;
import shamu.company.scheduler.job.SendEmailJob;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

public class SendEmailJobTests {
  private static SendEmailJob sendEmailJob;

  @Mock private EmailHelper emailHelper;

  @Mock private EmailService emailService;

  @Mock private JobExecutionContext jobExecutionContext;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    sendEmailJob = new SendEmailJob(emailHelper, emailService);
  }

  @Nested
  class executeJob {
    Email email;

    @BeforeEach
    void setUp() {
      email = new Email();
      email.setMessageId(UuidUtil.getUuidString());
    }

    @Test
    void whenJobExecutionContextIsValid_thenShouldSuccess() {
      final Map<String, Object> jobParameter = new HashMap<>();
      jobParameter.put("email", JsonUtil.formatToString(email));
      jobParameter.put("companyId", JsonUtil.formatToString(UuidUtil.getUuidString()));
      Mockito.when(jobExecutionContext.getMergedJobDataMap())
          .thenReturn(new JobDataMap(jobParameter));
      assertThatCode(() -> sendEmailJob.executeInternal(jobExecutionContext))
          .doesNotThrowAnyException();
    }
  }
}
