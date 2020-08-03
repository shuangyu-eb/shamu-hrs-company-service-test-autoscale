package shamu.company.scheduler;

import java.util.HashMap;
import java.util.Map;
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
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.PayPeriodFrequencyService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.scheduler.job.AddPayPeriodJob;
import shamu.company.user.service.UserCompensationService;
import shamu.company.utils.JsonUtil;

import static org.assertj.core.api.Assertions.assertThatCode;

public class AddPayPeriodJobTests {
  private static AddPayPeriodJob addPayPeriodJob;

  @Mock private AttendanceSetUpService attendanceSetUpService;
  @Mock private TimePeriodService timePeriodService;
  @Mock private AttendanceSettingsService attendanceSettingsService;
  @Mock private PayPeriodFrequencyService payPeriodFrequencyService;
  @Mock private JobExecutionContext jobExecutionContext;
  @Mock private UserCompensationService userCompensationService;
  @Mock private TimeSheetService timeSheetService;
  @Mock private CompanyService companyService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    addPayPeriodJob =
        new AddPayPeriodJob(
            attendanceSetUpService,
            timePeriodService,
            attendanceSettingsService,
            payPeriodFrequencyService,
            userCompensationService,
            timeSheetService,
            companyService);
  }

  @Nested
  class executeJob {
    String companyId;
    CompanyTaSetting companyTaSetting;
    TimePeriod timePeriod;
    StaticCompanyPayFrequencyType payFrequencyType;
    Company company;

    @BeforeEach
    void setUp() {
      companyId = "test_company_id";
      companyTaSetting = new CompanyTaSetting();
      timePeriod = new TimePeriod();
      company = new Company();

      payFrequencyType = new StaticCompanyPayFrequencyType();
      payFrequencyType.setName("WEEKLY");
      payFrequencyType.setId("id");
      companyTaSetting.setPayFrequencyType(payFrequencyType);
      Mockito.when(companyService.findById(Mockito.anyString())).thenReturn(company);
    }

    @Test
    void whenCompanyIdIsValid_thenShouldSucceed() {
      final Map<String, Object> jobParameter = new HashMap<>();
      jobParameter.put("companyId", JsonUtil.formatToString(companyId));
      Mockito.when(jobExecutionContext.getMergedJobDataMap())
          .thenReturn(new JobDataMap(jobParameter));
      Mockito.when(timePeriodService.findCompanyCurrentPeriod(companyId)).thenReturn(timePeriod);
      Mockito.when(attendanceSettingsService.findCompanySettings(companyId))
          .thenReturn(companyTaSetting);
      Mockito.when(payPeriodFrequencyService.findById("id")).thenReturn(payFrequencyType);
      Mockito.when(attendanceSetUpService.getNextPeriod(timePeriod, "WEEKLY", company))
          .thenReturn(timePeriod);
      assertThatCode(() -> addPayPeriodJob.executeInternal(jobExecutionContext))
          .doesNotThrowAnyException();
    }
  }
}
