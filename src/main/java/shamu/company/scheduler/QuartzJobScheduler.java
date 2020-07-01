package shamu.company.scheduler;

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
import shamu.company.utils.JsonUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
public class QuartzJobScheduler {

  private final Scheduler scheduler;
  private static final String ONCE_TIME_JOB_GROUP = "ONCE_TIME_JOB_GROUP";
  private static final String ONCE_TIME_TRIGGER_GROUP = "ONCE_TIME_TRIGGER_GROUP";

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
    final String triggerName = jobName + "_TRIGGER";

    final JobDetail jobDetail =
        assembleJobDetail(jobClass, jobName, formatValuesToString(jobParameter));

    final SimpleTrigger trigger = assembleSimpleTrigger(jobName, triggerName, startDate);

    final TriggerKey triggerKey = new TriggerKey(triggerName, ONCE_TIME_TRIGGER_GROUP);
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

  private JobDetail assembleJobDetail(
      final Class<? extends QuartzJobBean> jobClass,
      final String jobName,
      final Map<String, String> jobParameter) {
    return newJob(jobClass)
        .withIdentity(jobName, ONCE_TIME_JOB_GROUP)
        .usingJobData(new JobDataMap(jobParameter))
        .build();
  }

  private SimpleTrigger assembleSimpleTrigger(
      final String jobName, final String triggerName, final Date startDate) {
    return (SimpleTrigger)newTrigger()
        .withIdentity(triggerName, ONCE_TIME_TRIGGER_GROUP)
        .startAt(startDate)
        .forJob(jobName, ONCE_TIME_JOB_GROUP)
        .build();
  }

  private Map<String, String> formatValuesToString(final Map<String, Object> map) {
    final Map<String, String> formatMap = new HashMap<>();
    map.forEach((key, value) -> formatMap.put(key, JsonUtil.formatToString(value)));
    return formatMap;
  }
}
