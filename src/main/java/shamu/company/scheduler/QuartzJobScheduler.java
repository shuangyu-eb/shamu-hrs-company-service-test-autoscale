package shamu.company.scheduler;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;
import java.util.Map;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import shamu.company.scheduler.exception.QuartzException;

@Component
public class QuartzJobScheduler {

  private final Scheduler scheduler;

  @Autowired
  QuartzJobScheduler(final Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  /**
   * The job will be executed only once at the startDate. If the startDate is missed, the job will
   * be executed as soon as the service restarted.
   */
  public void addOrUpdateJobSchedule(
      final Class<? extends QuartzJobBean> jobClass,
      final String jobName,
      final Map<String, Object> jobParameter,
      final Date startDate) {
    final String jobGroupName = "ONCE_TIME_JOB_GROUP";
    final String triggerGroupName = "ONCE_TIME_TRIGGER_GROUP";
    final String triggerName = jobName + "_TRIGGER";

    final JobDetail jobDetail =
        newJob(jobClass)
            .withIdentity(jobName, jobGroupName)
            .usingJobData(new JobDataMap(jobParameter))
            .build();

    final SimpleTrigger trigger =
        (SimpleTrigger)
            newTrigger()
                .withIdentity(triggerName, triggerGroupName)
                .startAt(startDate)
                .forJob(jobName, jobGroupName)
                .build();

    final TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroupName);
    try {
      if (scheduler.checkExists(triggerKey)) {
        scheduler.rescheduleJob(triggerKey, trigger);
      } else {
        scheduler.scheduleJob(jobDetail, trigger);
      }
    } catch (final SchedulerException e) {
      throw new QuartzException("Schedule task failed.", e);
    }
  }
}
