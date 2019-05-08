package shamu.company.email;

import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
