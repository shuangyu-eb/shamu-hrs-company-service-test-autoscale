package shamu.company.scheduler.job;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.PayPeriodFrequencyService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.common.entity.PayrollDetail;
import shamu.company.common.service.PayrollDetailService;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.scheduler.QuartzUtil;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.service.UserCompensationService;

import java.sql.Timestamp;
import java.util.List;

import static shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;

public class AddPayPeriodJob extends QuartzJobBean {
  private static final long MS_OF_ONE_DAY = 24 * 60 * 60 * 1000L;
  private static final int DAYS_PAY_DATE_AFTER_PERIOD = 6;

  private final AttendanceSetUpService attendanceSetUpService;
  private final TimePeriodService timePeriodService;
  private final AttendanceSettingsService attendanceSettingsService;
  private final PayPeriodFrequencyService payPeriodFrequencyService;
  private final UserCompensationService userCompensationService;
  private final CompanyService companyService;
  private final PayrollDetailService payrollDetailService;

  @Autowired
  public AddPayPeriodJob(
      final AttendanceSetUpService attendanceSetUpService,
      final TimePeriodService timePeriodService,
      final AttendanceSettingsService attendanceSettingsService,
      final PayPeriodFrequencyService payPeriodFrequencyService,
      final UserCompensationService userCompensationService,
      final CompanyService companyService,
      final PayrollDetailService payrollDetailService) {
    this.attendanceSetUpService = attendanceSetUpService;
    this.timePeriodService = timePeriodService;
    this.attendanceSettingsService = attendanceSettingsService;
    this.payPeriodFrequencyService = payPeriodFrequencyService;
    this.userCompensationService = userCompensationService;
    this.companyService = companyService;
    this.payrollDetailService = payrollDetailService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {
    String companyId =
        QuartzUtil.getParameter(jobExecutionContext, "companyId", String.class);
    companyId = JsonUtil.deserialize(companyIdJson, String.class).replace("\"", "");
    TenantContext.withInTenant(
        companyId,
        () -> {
          final CompanyTaSetting companyTaSetting =
              attendanceSettingsService.findCompanySetting();
          final PayrollDetail payrollDetail = payrollDetailService.findByCompanyId(companyId);
          final String payFrequencyTypeId = payrollDetail.getPayFrequencyType().getId();
          final StaticCompanyPayFrequencyType staticCompanyPayFrequencyType =
              payPeriodFrequencyService.findById(payFrequencyTypeId);

          final TimePeriod nextTimePeriod =
              timePeriodService.save(
                  attendanceSetUpService.getNextPeriod(
                      currentTimePeriod, staticCompanyPayFrequencyType.getName(), company));
          final Timestamp nextPeriodEndDate = nextTimePeriod.getEndDate();
          final Timestamp nextPayDate =
              new Timestamp(nextPeriodEndDate.getTime() + DAYS_PAY_DATE_AFTER_PERIOD * MS_OF_ONE_DAY);
          payrollDetail.setLastPayrollPayday(nextPayDate);
          attendanceSettingsService.saveCompanyTaSetting(companyTaSetting);
          payrollDetailService.savePayrollDetail(payrollDetail);

          final List<UserCompensation> userCompensationList =
              userCompensationService.listNewestEnrolledCompensation();
          attendanceSetUpService.createTimeSheets(
              nextTimePeriod, TimeSheetStatus.ACTIVE, userCompensationList);
          attendanceSetUpService.scheduleTasks(
              companyId, nextTimePeriod, companyTaSetting.getTimeZone().getName());
        });
  }
}
