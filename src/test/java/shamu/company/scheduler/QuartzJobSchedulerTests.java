package shamu.company.scheduler;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.HashMap;

public class QuartzJobSchedulerTests {
  private static QuartzJobScheduler quartzJobScheduler;

  @Mock private Scheduler scheduler;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    quartzJobScheduler = new QuartzJobScheduler(scheduler);
  }

  @Nested
  class scheduleJob {
    String jobName;
    String triggerGroupName;

    @BeforeEach
    void setUp() {
      jobName = "JobForTest";
      triggerGroupName = "ONCE_TIME_TRIGGER_GROUP";
    }

    @Test
    void whenJobKeyNotExist_thenShouldSuccess() {
      Assertions.assertDoesNotThrow(
          () ->
              quartzJobScheduler.addOrUpdateJobSchedule(
                  JobForTest.class, jobName, triggerGroupName, new HashMap<>(), new Date()));
    }

    @Test
    void whenJobKeyExist_thenShouldSuccess() throws SchedulerException {
      Mockito.when(scheduler.checkExists(new TriggerKey(jobName + "_TRIGGER", triggerGroupName)))
          .thenReturn(true);
      Assertions.assertDoesNotThrow(
          () ->
              quartzJobScheduler.addOrUpdateJobSchedule(
                  JobForTest.class, jobName, triggerGroupName, new HashMap<>(), new Date()));
    }
  }

  static class JobForTest extends QuartzJobBean {

    @Override
    protected void executeInternal(@NotNull final JobExecutionContext jobExecutionContext) {}
  }
}
