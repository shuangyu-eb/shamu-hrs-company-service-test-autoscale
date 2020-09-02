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
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.scheduler.job.ChangeTimeSheetsStatusJob;
import shamu.company.utils.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeTimeSheetsStatusJobTests {
  private static ChangeTimeSheetsStatusJob changeTimeSheetsStatusJob;
  @Mock private TimePeriodService timePeriodService;
  @Mock private TimeSheetService timeSheetService;
  @Mock private JobExecutionContext jobExecutionContext;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    changeTimeSheetsStatusJob = new ChangeTimeSheetsStatusJob(timeSheetService);
  }

  @Nested
  class executeJob {
    TimePeriod timePeriod = new TimePeriod();
    String timePeriodId = "period_id";
    StaticTimesheetStatus timeSheetStatus = new StaticTimesheetStatus();
    TimeSheet timeSheet = new TimeSheet();

    @BeforeEach
    void init() {
      timePeriod.setId(timePeriodId);
      timeSheetStatus.setName("status_name");
      timeSheet.setStatus(timeSheetStatus);
    }

    @Test
    void whenJobExecutionContextIsValid_thenShouldSuccess() {
      final Map<String, Object> jobParameter = new HashMap<>();
      jobParameter.put("timePeriodId", JsonUtil.formatToString(timePeriodId));
      jobParameter.put(
          "fromStatus",
          JsonUtil.formatToString(StaticTimesheetStatus.TimeSheetStatus.NOT_YET_START.name()));
      jobParameter.put(
          "toStatus", JsonUtil.formatToString(StaticTimesheetStatus.TimeSheetStatus.ACTIVE.name()));
      Mockito.when(jobExecutionContext.getMergedJobDataMap())
          .thenReturn(new JobDataMap(jobParameter));

      Mockito.when(timePeriodService.findById(timePeriodId)).thenReturn(timePeriod);
      final List<TimeSheet> timeSheetList = new ArrayList<>();
      timeSheetList.add(timeSheet);
      Mockito.when(timeSheetService.findAllByPeriodId(timePeriodId)).thenReturn(timeSheetList);
      Assertions.assertDoesNotThrow(
          () -> changeTimeSheetsStatusJob.executeInternal(jobExecutionContext));
    }
  }
}
