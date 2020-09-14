package shamu.company.scheduler.job;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.scheduler.QuartzUtil;

public class ChangeTimeSheetsStatusJob extends QuartzJobBean {
  private final TimeSheetService timeSheetService;

  @Autowired
  public ChangeTimeSheetsStatusJob(final TimeSheetService timeSheetService) {
    this.timeSheetService = timeSheetService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {
    final String fromStatus =
        QuartzUtil.getParameter(jobExecutionContext, "fromStatus", String.class);
    final String toStatus = QuartzUtil.getParameter(jobExecutionContext, "toStatus", String.class);
    final String timePeriodId =
        QuartzUtil.getParameter(jobExecutionContext, "timePeriodId", String.class);
    final String companyId =
        QuartzUtil.getParameter(jobExecutionContext, "companyId", String.class);

    TenantContext.withInTenant(companyId, () -> timeSheetService.updateCompanyTimeSheetsStatus(fromStatus, toStatus, timePeriodId));
  }
}
