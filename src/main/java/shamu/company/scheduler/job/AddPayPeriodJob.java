package shamu.company.scheduler.job;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.CompanyTaSettingService;
import shamu.company.attendance.service.PayPeriodFrequencyService;
import shamu.company.attendance.service.TimePeriodService;

public class AddPayPeriodJob extends QuartzJobBean {
  private final AttendanceSetUpService attendanceSetUpService;
  private final TimePeriodService timePeriodService;
  private final CompanyTaSettingService companyTaSettingService;
  private final PayPeriodFrequencyService payPeriodFrequencyService;

  @Autowired
  public AddPayPeriodJob(
      final AttendanceSetUpService attendanceSetUpService,
      final TimePeriodService timePeriodService,
      final CompanyTaSettingService companyTaSettingService,
      final PayPeriodFrequencyService payPeriodFrequencyService) {
    this.attendanceSetUpService = attendanceSetUpService;
    this.timePeriodService = timePeriodService;
    this.companyTaSettingService = companyTaSettingService;
    this.payPeriodFrequencyService = payPeriodFrequencyService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {

    final String companyId = (String) jobExecutionContext.getMergedJobDataMap().get("companyId");
    final TimePeriod currentTimePeriod = timePeriodService.findCompanyCurrentPeriod(companyId);

    final CompanyTaSetting companyTaSetting = companyTaSettingService.findByCompany(companyId);
    final String payFrequencyTypeId = companyTaSetting.getPayFrequencyType().getId();
    final StaticCompanyPayFrequencyType staticCompanyPayFrequencyType =
        payPeriodFrequencyService.findById(payFrequencyTypeId);

    final TimePeriod nextTimePeriod =
        attendanceSetUpService.getNextPeriod(
            currentTimePeriod, staticCompanyPayFrequencyType.getName());

    attendanceSetUpService.createTimeSheetsAndPeriod(companyId, nextTimePeriod);
    attendanceSetUpService.scheduleNextPeriod(companyId, nextTimePeriod.getEndDate());
  }
}
