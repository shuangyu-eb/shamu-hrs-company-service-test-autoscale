package shamu.company.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.CompanyTaSettingService;
import shamu.company.attendance.service.PayPeriodFrequencyService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.scheduler.job.AddPayPeriodJob;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;

public class AddPayPeriodJobTests {
  private static AddPayPeriodJob addPayPeriodJob;

  @Mock private AttendanceSetUpService attendanceSetUpService;
  @Mock private TimePeriodService timePeriodService;
  @Mock private CompanyTaSettingService companyTaSettingService;
  @Mock private PayPeriodFrequencyService payPeriodFrequencyService;
  @Mock private JobExecutionContext jobExecutionContext;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    addPayPeriodJob =
        new AddPayPeriodJob(
            attendanceSetUpService,
            timePeriodService,
            companyTaSettingService,
            payPeriodFrequencyService);
  }

  @Nested
  class executeJob {
    String companyId;
    CompanyTaSetting companyTaSetting;
    TimePeriod timePeriod;
    StaticCompanyPayFrequencyType payFrequencyType;

    @BeforeEach
    void setUp() {
      companyId = "test_company_id";
      companyTaSetting = new CompanyTaSetting();
      timePeriod = new TimePeriod();

      payFrequencyType = new StaticCompanyPayFrequencyType();
      payFrequencyType.setName("WEEKLY");
      payFrequencyType.setId("id");
      companyTaSetting.setPayFrequencyType(payFrequencyType);
    }

    @Test
    void whenCompanyIdIsValid_thenShouldSucceed() {
      final Map<String, Object> jobParameter = new HashMap<>();
      jobParameter.put("companyId", companyId);
      Mockito.when(jobExecutionContext.getMergedJobDataMap())
          .thenReturn(new JobDataMap(jobParameter));
      Mockito.when(timePeriodService.findCompanyCurrentPeriod(companyId)).thenReturn(timePeriod);
      Mockito.when(companyTaSettingService.findByCompany(companyId)).thenReturn(companyTaSetting);
      Mockito.when(payPeriodFrequencyService.findById("id")).thenReturn(payFrequencyType);
      Mockito.when(attendanceSetUpService.getNextPeriod(timePeriod, "WEEKLY"))
          .thenReturn(timePeriod);
      assertThatCode(() -> addPayPeriodJob.executeInternal(jobExecutionContext))
          .doesNotThrowAnyException();
    }
  }
}
