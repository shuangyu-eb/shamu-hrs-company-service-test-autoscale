package shamu.company.scheduler.job;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.scheduler.QuartzUtil;

import static shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;

public class AutoSubmitTimeSheetsJob extends QuartzJobBean {
  private final TimeSheetService timeSheetService;

  @Autowired
  public AutoSubmitTimeSheetsJob(final TimeSheetService timeSheetService) {
    this.timeSheetService = timeSheetService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {
    final String companyId =
        QuartzUtil.getParameter(jobExecutionContext, "companyId", String.class);

    timeSheetService.updateCompanyLastPeriodTimeSheetsStatus(
        companyId, TimeSheetStatus.ACTIVE.name(), TimeSheetStatus.SUBMITTED.name());
  }
}
