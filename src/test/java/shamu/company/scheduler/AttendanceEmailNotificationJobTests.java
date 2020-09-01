package shamu.company.scheduler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.email.service.EmailService;
import shamu.company.scheduler.job.AttendanceEmailNotificationJob;
import shamu.company.utils.JsonUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AttendanceEmailNotificationJobTests {
  private static AttendanceEmailNotificationJob attendanceEmailNotificationJob;
  @Mock private AttendanceSetUpService attendanceSetUpService;
  @Mock private JobExecutionContext jobExecutionContext;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    attendanceEmailNotificationJob = new AttendanceEmailNotificationJob(attendanceSetUpService);
  }

  @Nested
  class executeJob {
    String periodId = "test_period_id";
    Date sendDate = new Date();

    @Test
    void whenJobExecutionContextIsValid_thenShouldSuccess() {
      final Map<String, Object> jobParameter = new HashMap<>();
      jobParameter.put("periodId", JsonUtil.formatToString(periodId));
      jobParameter.put(
          "emailNotification",
          JsonUtil.formatToString(EmailService.EmailNotification.SUBMIT_TIME_SHEET));
      jobParameter.put("sendDate", JsonUtil.formatToString(sendDate));
      Mockito.when(jobExecutionContext.getMergedJobDataMap())
          .thenReturn(new JobDataMap(jobParameter));
      Assertions.assertDoesNotThrow(
          () -> attendanceEmailNotificationJob.executeInternal(jobExecutionContext));
    }
  }
}
