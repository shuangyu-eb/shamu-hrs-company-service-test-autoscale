package shamu.company.scheduler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import shamu.company.email.service.EmailService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class DynamicSchedulerTests {

  private final ScheduledTaskRegistrar scheduledTaskRegistrar = Mockito.mock(ScheduledTaskRegistrar.class);

  private final Map<String, ScheduledFuture<?>> taskFutures = new HashMap<>();

  final ScheduledFuture<EmailService> future = Mockito.mock(ScheduledFuture.class);

  private DynamicScheduler dynamicScheduler;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    dynamicScheduler = new DynamicScheduler();
  }

  @Test
  void testConfigureTasks() {
    Assertions.assertDoesNotThrow(() -> dynamicScheduler.configureTasks(scheduledTaskRegistrar));
  }

  @Nested
  class testAddTriggerTask {

    final Date date = new Date();
    final Runnable runnable = Mockito.mock(Runnable.class);

    @Test
    void whenTaskContainsId_thenShouldThrow() {

      taskFutures.put("1",future);
      Whitebox.setInternalState(dynamicScheduler,"taskFutures",taskFutures);
      Assertions.assertThrows(
          SchedulingException.class, () -> dynamicScheduler.addTriggerTask("1", runnable, date));
    }

    @Test
    void whenTaskNotContainsId_thenShouldNotThrow() {
      final TaskScheduler scheduler = Mockito.mock(TaskScheduler.class);
      Mockito.when(scheduledTaskRegistrar.getScheduler()).thenReturn(scheduler);
      taskFutures.put("2",future);
      Whitebox.setInternalState(dynamicScheduler,"taskFutures",taskFutures);
      Whitebox.setInternalState(dynamicScheduler,"scheduledTaskRegistrar",scheduledTaskRegistrar);
      Assertions.assertDoesNotThrow(() -> dynamicScheduler.addTriggerTask("1",runnable,date));
    }
}
   @Test
   void testCancelTriggerTask() {
     taskFutures.put("1",future);
     Whitebox.setInternalState(dynamicScheduler,"taskFutures",taskFutures);
     Assertions.assertDoesNotThrow(() -> dynamicScheduler.cancelTriggerTask("1"));
   }

   @Test
   void testGetTaskIds() {
     taskFutures.put("1",future);
     Whitebox.setInternalState(dynamicScheduler,"taskFutures",taskFutures);
     Assertions.assertNotNull(dynamicScheduler.getTaskIds());
   }

   @Test
   void testHasTask() {
     taskFutures.put("1",future);
     Whitebox.setInternalState(dynamicScheduler,"taskFutures",taskFutures);
     Assertions.assertTrue(dynamicScheduler.hasTask("1"));
   }

   @Test
   void testUpdateOrAddUniqueTriggerTask() {
     taskFutures.put("1",future);
     final Date date = new Date();
     final Runnable runnable = Mockito.mock(Runnable.class);
     final TaskScheduler scheduler = Mockito.mock(TaskScheduler.class);
     Mockito.when(scheduledTaskRegistrar.getScheduler()).thenReturn(scheduler);
     Whitebox.setInternalState(dynamicScheduler,"taskFutures",taskFutures);
     Whitebox.setInternalState(dynamicScheduler,"scheduledTaskRegistrar",scheduledTaskRegistrar);
     Assertions.assertDoesNotThrow(() -> dynamicScheduler.updateOrAddUniqueTriggerTask("1", runnable, date));
   }
}
