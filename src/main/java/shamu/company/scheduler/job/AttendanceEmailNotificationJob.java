package shamu.company.scheduler.job;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.scheduler.QuartzUtil;

import java.util.Date;

import static shamu.company.email.service.EmailService.EmailNotification;

public class AttendanceEmailNotificationJob extends QuartzJobBean {
  private final AttendanceSetUpService attendanceSetUpService;

  @Autowired
  public AttendanceEmailNotificationJob(final AttendanceSetUpService attendanceSetUpService) {
    this.attendanceSetUpService = attendanceSetUpService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {
    final String periodId = QuartzUtil.getParameter(jobExecutionContext, "periodId", String.class);
    final EmailNotification emailNotification =
        QuartzUtil.getParameter(jobExecutionContext, "emailNotification", EmailNotification.class);
    final Date sendDate = QuartzUtil.getParameter(jobExecutionContext, "sendDate", Date.class);
    final String companyId =
        QuartzUtil.getParameter(jobExecutionContext, "companyId", String.class);

    TenantContext.withInTenant(companyId, () -> attendanceSetUpService.sendEmailNotification(periodId, emailNotification, sendDate));
  }
}
