package shamu.company.email;

import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class EmailWorker implements InitializingBean {

  @Autowired
  EmailService emailService;

  @Autowired
  TaskScheduler taskScheduler;

  @Override
  public void afterPropertiesSet() {
    List<Email> emailTaskList = emailService.findAllUnfinishedTasks();
    emailTaskList.forEach(
        emailTask -> {
          Runnable emailRunnable = emailService.getEmailTask(emailTask);
          taskScheduler.schedule(emailRunnable, emailTask.getSendDate());
        });
  }
}