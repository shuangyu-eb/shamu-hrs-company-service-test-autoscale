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
import shamu.company.utils.JsonUtil;

import static shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;

public class AddPayPeriodJob extends QuartzJobBean {
  private final AttendanceSetUpService attendanceSetUpService;
  private final TimePeriodService timePeriodService;
  private final AttendanceSettingsService attendanceSettingsService;
  private final PayPeriodFrequencyService payPeriodFrequencyService;

  @Autowired
  public AddPayPeriodJob(
      final AttendanceSetUpService attendanceSetUpService,
      final TimePeriodService timePeriodService,
      final AttendanceSettingsService attendanceSettingsService,
      final PayPeriodFrequencyService payPeriodFrequencyService) {
    this.attendanceSetUpService = attendanceSetUpService;
    this.timePeriodService = timePeriodService;
    this.attendanceSettingsService = attendanceSettingsService;
    this.payPeriodFrequencyService = payPeriodFrequencyService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {

    final String companyIdJson =
        String.valueOf(jobExecutionContext.getMergedJobDataMap().get("companyId"));
    final String companyId = JsonUtil.deserialize(companyIdJson, String.class);
    final TimePeriod currentTimePeriod = timePeriodService.findCompanyCurrentPeriod(companyId);

    final CompanyTaSetting companyTaSetting =
        attendanceSettingsService.findCompanySettings(companyId);
    final String payFrequencyTypeId = companyTaSetting.getPayFrequencyType().getId();
    final StaticCompanyPayFrequencyType staticCompanyPayFrequencyType =
        payPeriodFrequencyService.findById(payFrequencyTypeId);

    final TimePeriod nextTimePeriod =
        attendanceSetUpService.getNextPeriod(
            currentTimePeriod, staticCompanyPayFrequencyType.getName());

    attendanceSetUpService.createTimeSheetsAndPeriod(
        companyId, nextTimePeriod, TimeSheetStatus.ACTIVE);
    attendanceSetUpService.scheduleCreateNextPeriod(companyId, nextTimePeriod.getEndDate());
  }
}
