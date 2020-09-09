package shamu.company.scheduler;

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
import shamu.company.scheduler.job.SendEmailsJob;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;

public class SendEmailsJobTests {

  private static SendEmailsJob sendEmailsJob;

  @Mock private EmailHelper emailHelper;

  @Mock private EmailService emailService;

  @Mock private JobExecutionContext jobExecutionContext;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    sendEmailsJob = new SendEmailsJob(emailHelper, emailService);
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
      final List<String> messageIdList = new ArrayList<>();
      messageIdList.add("test_message_id");
      jobParameter.put("messageIdList", JsonUtil.formatToString(messageIdList));
      Mockito.when(jobExecutionContext.getMergedJobDataMap())
          .thenReturn(new JobDataMap(jobParameter));
      assertThatCode(() -> sendEmailsJob.executeInternal(jobExecutionContext))
          .doesNotThrowAnyException();
    }
  }
}
