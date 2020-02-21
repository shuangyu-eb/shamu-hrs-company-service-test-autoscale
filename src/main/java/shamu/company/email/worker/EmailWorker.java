package shamu.company.email.worker;

import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shamu.company.email.entity.Email;
import shamu.company.email.service.EmailService;

@Component
public class EmailWorker implements InitializingBean {

  private final EmailService emailService;

  @Autowired
  public EmailWorker(EmailService emailService) {
    this.emailService = emailService;
  }

  @Override
  public void afterPropertiesSet() {
    List<Email> emailTaskList = emailService.findAllUnfinishedTasks();
    emailTaskList.forEach(
        emailService::scheduleEmail);
  }
}
