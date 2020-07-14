package shamu.company.scheduler.job;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.utils.JsonUtil;

import java.util.List;

public class ActivateTimeSheetJob extends QuartzJobBean {
  private final TimeSheetService timeSheetService;
  private final StaticTimesheetStatusRepository staticTimesheetStatusRepository;

  @Autowired
  ActivateTimeSheetJob(
      final TimeSheetService timeSheetService,
      final StaticTimesheetStatusRepository staticTimesheetStatusRepository) {
    this.timeSheetService = timeSheetService;
    this.staticTimesheetStatusRepository = staticTimesheetStatusRepository;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {
    final String companyIdJson =
        String.valueOf(jobExecutionContext.getMergedJobDataMap().get("companyId"));
    final String companyId = JsonUtil.deserialize(companyIdJson, String.class);
    final List<TimeSheet> timeSheets = timeSheetService.listByCompany(companyId);
    final StaticTimesheetStatus staticTimesheetStatus =
        staticTimesheetStatusRepository.findByName(TimeSheetStatus.ACTIVE.getValue());
    timeSheets.forEach(timeSheet -> timeSheet.setStatus(staticTimesheetStatus));
    timeSheetService.saveAll(timeSheets);
  }
}
