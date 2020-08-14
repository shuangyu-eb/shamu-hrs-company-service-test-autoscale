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
import shamu.company.attendance.repository.TimeSheetRepository;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.scheduler.job.AutoSubmitTimeSheetsJob;
import shamu.company.utils.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoSubmitTimeSheetsJobTests {
  private static AutoSubmitTimeSheetsJob autoSubmitTimeSheetsJob;
  @Mock private TimePeriodService timePeriodService;
  @Mock private TimeSheetService timeSheetService;
  @Mock private TimeSheetRepository timeSheetRepository;
  @Mock private JobExecutionContext jobExecutionContext;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    autoSubmitTimeSheetsJob = new AutoSubmitTimeSheetsJob(timePeriodService, timeSheetService);
  }

  @Nested
  class executeJob {
    String companyId;
    TimePeriod timePeriod = new TimePeriod();
    String timePeriodId = "period_id";
    StaticTimesheetStatus timeSheetStatus = new StaticTimesheetStatus();
    TimeSheet timeSheet = new TimeSheet();

    @BeforeEach
    void init() {
      companyId = "test_company_id";
      timePeriod.setId(timePeriodId);
      timeSheetStatus.setName("status_name");
      timeSheet.setStatus(timeSheetStatus);
    }

    @Test
    void whenJobExecutionContextIsValid_thenShouldSuccess() {
      final Map<String, Object> jobParameter = new HashMap<>();
      jobParameter.put("companyId", JsonUtil.formatToString(companyId));
      Mockito.when(jobExecutionContext.getMergedJobDataMap())
          .thenReturn(new JobDataMap(jobParameter));

      Mockito.when(timePeriodService.findCompanyCurrentPeriod(companyId)).thenReturn(timePeriod);
      final List<TimeSheet> timeSheetList = new ArrayList<>();
      timeSheetList.add(timeSheet);
      Mockito.when(timeSheetService.findAllByPeriodId(timePeriodId)).thenReturn(timeSheetList);
      Assertions.assertDoesNotThrow(
          () -> autoSubmitTimeSheetsJob.executeInternal(jobExecutionContext));
    }
  }
}
