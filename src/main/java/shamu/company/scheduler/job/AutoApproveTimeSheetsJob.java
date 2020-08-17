package shamu.company.scheduler.job;

import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.scheduler.QuartzUtil;

import java.util.Date;

public class AutoApproveTimeSheetsJob extends QuartzJobBean {
  private final AttendanceSetUpService attendanceSetUpService;
  private final TimePeriodService timePeriodService;
  private final AttendanceSettingsService attendanceSettingsService;
  private final TimeSheetService timeSheetService;

  public AutoApproveTimeSheetsJob(
      final AttendanceSetUpService attendanceSetUpService,
      final TimePeriodService timePeriodService,
      final AttendanceSettingsService attendanceSettingsService,
      final TimeSheetService timeSheetService) {
    this.attendanceSetUpService = attendanceSetUpService;
    this.timePeriodService = timePeriodService;
    this.attendanceSettingsService = attendanceSettingsService;
    this.timeSheetService = timeSheetService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {
    final String companyId =
        QuartzUtil.getParameter(jobExecutionContext, "companyId", String.class);

    timeSheetService.updateCompanyLastPeriodTimeSheetsStatus(
        companyId, TimeSheetStatus.SUBMITTED.name(), TimeSheetStatus.APPROVED.name());

    final CompanyTaSetting companyTaSetting =
        attendanceSettingsService.findCompanySettings(companyId);
    final TimePeriod currentTimePeriod = timePeriodService.findCompanyCurrentPeriod(companyId);
    final Date runPayRollDdl =
        attendanceSetUpService.getAutoApproveDate(
            companyId, currentTimePeriod.getEndDate(), companyTaSetting.getTimeZone().getName());
    attendanceSetUpService.scheduleAutoApproveTimeSheets(companyId, runPayRollDdl);
  }
}
