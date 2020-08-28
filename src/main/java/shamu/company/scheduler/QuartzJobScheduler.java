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
      String jobName,
      final String groupName,
      final Map<String, Object> jobParameter,
      final Date startDate) {
    jobName += "_" + startDate.getTime();

    final String triggerName = jobName;
    final SimpleTrigger trigger = assembleSimpleTrigger(jobName, groupName, triggerName, startDate);
    final TriggerKey triggerKey = new TriggerKey(triggerName, groupName);

    try {
      if (scheduler.checkExists(triggerKey)) {
        scheduler.rescheduleJob(triggerKey, trigger);
      } else {
        final JobDetail jobDetail =
            assembleJobDetail(jobClass, jobName, groupName, formatValuesToString(jobParameter));
        scheduler.scheduleJob(jobDetail, trigger);
      }
    } catch (final SchedulerException e) {
      throw new QuartzException("Schedule task failed." + e, e);
    }
  }

  private JobDetail assembleJobDetail(
      final Class<? extends QuartzJobBean> jobClass,
      final String jobName,
      final String groupName,
      final Map<String, String> jobParameter) {
    return newJob(jobClass)
        .withIdentity(jobName, groupName)
        .usingJobData(new JobDataMap(jobParameter))
        .build();
  }

  private SimpleTrigger assembleSimpleTrigger(
      final String jobName,
      final String groupName,
      final String triggerName,
      final Date startDate) {
    return (SimpleTrigger)
        newTrigger()
            .withIdentity(triggerName, groupName)
            .startAt(startDate)
            .forJob(jobName, groupName)
            .build();
  }

  private Map<String, String> formatValuesToString(final Map<String, Object> map) {
    final Map<String, String> formatMap = new HashMap<>();
    map.forEach((key, value) -> formatMap.put(key, JsonUtil.formatToString(value)));
    return formatMap;
  }
}
