package shamu.company.scheduler.job;

import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.scheduler.QuartzUtil;

public class AutoApproveTimeSheetsJob extends QuartzJobBean {
  private final TimeSheetService timeSheetService;

  public AutoApproveTimeSheetsJob(final TimeSheetService timeSheetService) {
    this.timeSheetService = timeSheetService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {
    final String companyId =
        QuartzUtil.getParameter(jobExecutionContext, "companyId", String.class);

    timeSheetService.updateCompanyLastPeriodTimeSheetsStatus(
        companyId, TimeSheetStatus.SUBMITTED.name(), TimeSheetStatus.APPROVED.name());
  }
}
