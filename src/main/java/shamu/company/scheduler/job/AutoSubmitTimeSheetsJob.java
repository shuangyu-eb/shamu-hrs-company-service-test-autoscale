package shamu.company.scheduler.job;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.scheduler.QuartzUtil;

import java.util.List;
import java.util.stream.Collectors;

public class AutoSubmitTimeSheetsJob extends QuartzJobBean {
  private final TimePeriodService timePeriodService;
  private final TimeSheetService timeSheetService;

  @Autowired
  public AutoSubmitTimeSheetsJob(
      final TimePeriodService timePeriodService, final TimeSheetService timeSheetService) {
    this.timePeriodService = timePeriodService;
    this.timeSheetService = timeSheetService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {
    final String companyId =
        QuartzUtil.getParameter(jobExecutionContext, "companyId", String.class);

    final TimePeriod lastTimePeriod = timePeriodService.findCompanyCurrentPeriod(companyId);
    final List<TimeSheet> timeSheetsToSubmit =
        timeSheetService.findAllByPeriodId(lastTimePeriod.getId()).stream()
            .filter(
                timeSheet ->
                    (timeSheet
                        .getStatus()
                        .getName()
                        .equals(StaticTimesheetStatus.TimeSheetStatus.ACTIVE.name())))
            .collect(Collectors.toList());

    timeSheetService.updateAllTimesheetStatus(timeSheetsToSubmit);
  }
}
