package shamu.company.scheduler.job;

import java.util.List;
import java.util.stream.Collectors;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.PayPeriodFrequencyService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.service.UserCompensationService;
import shamu.company.utils.JsonUtil;

import static shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;

public class AddPayPeriodJob extends QuartzJobBean {
  private final AttendanceSetUpService attendanceSetUpService;
  private final TimePeriodService timePeriodService;
  private final AttendanceSettingsService attendanceSettingsService;
  private final PayPeriodFrequencyService payPeriodFrequencyService;
  private final UserCompensationService userCompensationService;
  private final TimeSheetService timeSheetService;
  private final CompanyService companyService;

  @Autowired
  public AddPayPeriodJob(
      final AttendanceSetUpService attendanceSetUpService,
      final TimePeriodService timePeriodService,
      final AttendanceSettingsService attendanceSettingsService,
      final PayPeriodFrequencyService payPeriodFrequencyService,
      final UserCompensationService userCompensationService,
      final TimeSheetService timeSheetService,
      final CompanyService companyService) {
    this.attendanceSetUpService = attendanceSetUpService;
    this.timePeriodService = timePeriodService;
    this.attendanceSettingsService = attendanceSettingsService;
    this.payPeriodFrequencyService = payPeriodFrequencyService;
    this.userCompensationService = userCompensationService;
    this.timeSheetService = timeSheetService;
    this.companyService = companyService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {

    final String companyIdJson =
        String.valueOf(jobExecutionContext.getMergedJobDataMap().get("companyId"));
    final String companyId = JsonUtil.deserialize(companyIdJson, String.class);
    final TimePeriod currentTimePeriod = timePeriodService.findCompanyCurrentPeriod(companyId);

    final Company company = companyService.findById(companyId);
    final CompanyTaSetting companyTaSetting =
        attendanceSettingsService.findCompanySettings(companyId);
    final String payFrequencyTypeId = companyTaSetting.getPayFrequencyType().getId();
    final StaticCompanyPayFrequencyType staticCompanyPayFrequencyType =
        payPeriodFrequencyService.findById(payFrequencyTypeId);

    final TimePeriod lastTimePeriod = timePeriodService.findCompanyCurrentPeriod(companyId);
    final List<TimeSheet> timeSheetsToSubmit =
        timeSheetService.findAllByPeriodId(lastTimePeriod.getId()).stream()
            .filter(
                timeSheet ->
                    (timeSheet.getStatus().getName().equals(TimeSheetStatus.ACTIVE.name())))
            .collect(Collectors.toList());

    timeSheetService.updateAllTimesheetStatus(timeSheetsToSubmit);

    final TimePeriod nextTimePeriod =
        attendanceSetUpService.getNextPeriod(
            currentTimePeriod, staticCompanyPayFrequencyType.getName(), company);

    final List<UserCompensation> userCompensationList =
        userCompensationService.listNewestEnrolledCompensation(companyId);
    attendanceSetUpService.createTimeSheetsAndPeriod(
        nextTimePeriod, TimeSheetStatus.ACTIVE, userCompensationList);
    attendanceSetUpService.scheduleCreateNextPeriod(companyId, nextTimePeriod.getEndDate());
  }
}
