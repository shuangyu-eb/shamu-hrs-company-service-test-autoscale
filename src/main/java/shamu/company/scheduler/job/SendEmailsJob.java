package shamu.company.scheduler.job;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.email.entity.Email;
import shamu.company.email.service.EmailService;
import shamu.company.helpers.EmailHelper;
import shamu.company.scheduler.QuartzUtil;

public class SendEmailsJob extends QuartzJobBean {
  private final EmailHelper emailHelper;
  private final EmailService emailService;

  @Autowired
  public SendEmailsJob(final EmailHelper emailHelper, final EmailService emailService) {
    this.emailHelper = emailHelper;
    this.emailService = emailService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {
    final List<String> messageIdList =
        QuartzUtil.getParameter(jobExecutionContext, "messageIdList", ArrayList.class);
    final String companyId = QuartzUtil.getParameter(jobExecutionContext, "companyId", String.class);
    TenantContext.withInTenant(
        companyId,
        () -> {
          final List<Email> emails = emailService.listByMessageIds(messageIdList);
          try {
            emailHelper.send(emails);
            emails.forEach(email -> email.setSendDate(new Timestamp(new Date().getTime())));
            emailService.saveAll(emails);
          } catch (final Exception e) {
            emailService.rescheduleFailedEmails(emails);
          }
        });
  }
}
