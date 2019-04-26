package shamu.company.email;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import shamu.company.utils.EmailUtil;

@Service
public class EmailServiceImpl implements EmailService {

  @Autowired
  private EmailRepository emailRepository;

  @Autowired
  TaskScheduler taskScheduler;

  @Autowired
  EmailUtil emailUtil;

  @Override
  public Email save(Email email) {
    return emailRepository.save(email);
  }

  @Override
  public List<Email> findAllUnfinishedTasks() {
    return emailRepository.findAllUnfinishedTasks();
  }

  public void scheduleEmail(Email email) {
    taskScheduler.schedule(getEmailTask(email), email.getSendDate());
  }

  public Runnable getEmailTask(Email email) {
    return () -> {
      emailUtil.send(email);
      email.setSentAt(new Timestamp(new Date().getTime()));
      emailRepository.save(email);
    };
  }
}
