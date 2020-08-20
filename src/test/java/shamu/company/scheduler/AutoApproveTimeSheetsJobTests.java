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
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.scheduler.job.AutoApproveTimeSheetsJob;
import shamu.company.utils.JsonUtil;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AutoApproveTimeSheetsJobTests {
  private static AutoApproveTimeSheetsJob autoApproveTimeSheetsJob;
  @Mock private TimeSheetService timeSheetService;
  @Mock private JobExecutionContext jobExecutionContext;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    autoApproveTimeSheetsJob = new AutoApproveTimeSheetsJob(timeSheetService);
  }

  @Nested
  class executeJob {
    String companyId;
    CompanyTaSetting companyTaSetting = new CompanyTaSetting();
    TimePeriod timePeriod = new TimePeriod();
    Timestamp endDate = new Timestamp(new Date().getTime());
    Date runPayrollDdl = new Date();
    StaticTimezone timezone = new StaticTimezone();
    String timeZoneName = "Hongkong";

    @BeforeEach
    void init() {
      companyId = "test_company_id";
      timePeriod.setEndDate(endDate);
      timezone.setName(timeZoneName);
      companyTaSetting.setTimeZone(timezone);
    }

    @Test
    void whenJobExecutionContextIsValid_thenShouldSuccess() {
      final Map<String, Object> jobParameter = new HashMap<>();
      jobParameter.put("companyId", JsonUtil.formatToString(companyId));
      Mockito.when(jobExecutionContext.getMergedJobDataMap())
          .thenReturn(new JobDataMap(jobParameter));

      Assertions.assertDoesNotThrow(
          () -> autoApproveTimeSheetsJob.executeInternal(jobExecutionContext));
    }
  }
}
