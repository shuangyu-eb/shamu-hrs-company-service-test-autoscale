package shamu.company.scheduler;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;


@Service
public class DynamicScheduler implements SchedulingConfigurer {

  private ScheduledTaskRegistrar scheduledTaskRegistrar;

  private Map<String, ScheduledFuture<?>> taskFutures = new ConcurrentHashMap<>();

  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    this.scheduledTaskRegistrar = taskRegistrar;
  }

  public void addTriggerTask(String taskId, Runnable runnable, Date date) {
    if (taskFutures.containsKey(taskId)) {
      throw new SchedulingException("the taskId[" + taskId + "] was added.");
    }
    TaskScheduler scheduler = scheduledTaskRegistrar.getScheduler();
    ScheduledFuture<?> future = scheduler.schedule(runnable, date);
    taskFutures.put(taskId, future);
  }

  public void cancelTriggerTask(String taskId) {
    ScheduledFuture<?> future = taskFutures.get(taskId);
    if (future != null) {
      future.cancel(true);
    }
    taskFutures.remove(taskId);
  }

  public Set<String> getTaskIds() {
    return taskFutures.keySet();
  }

  public boolean hasTask(String taskId) {
    return this.taskFutures.containsKey(taskId);
  }

  public void updateOrAddUniqueTriggerTask(String taskId, Runnable task, Date startTime) {
    if (hasTask(taskId)) {
      cancelTriggerTask(taskId);
    }
    addTriggerTask(taskId, task, startTime);
  }
}
