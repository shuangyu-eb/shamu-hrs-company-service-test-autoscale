package shamu.company.scheduler.job;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.email.entity.Email;
import shamu.company.email.service.EmailService;
import shamu.company.helpers.EmailHelper;
import shamu.company.scheduler.QuartzUtil;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

public class SendEmailJob extends QuartzJobBean {
  private final EmailHelper emailHelper;
  private final EmailService emailService;

  @Autowired
  public SendEmailJob(final EmailHelper emailHelper, final EmailService emailService) {
    this.emailHelper = emailHelper;
    this.emailService = emailService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {
    final String emailId = QuartzUtil.getParameter(jobExecutionContext, "emailId", String.class);
    final Email email = emailService.get(emailId);
    final String companyId = QuartzUtil.getParameter(jobExecutionContext, "companyId", String.class);
    TenantContext.setCurrentTenant(companyId);
    try {
      emailHelper.send(email);
      email.setSendDate(new Timestamp(new Date().getTime()));
      emailService.save(email);
    } catch (final Exception e) {
      emailService.rescheduleFailedEmails(Arrays.asList(email));
    }
    TenantContext.clear();
  }
}
