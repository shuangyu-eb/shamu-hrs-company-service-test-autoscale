package shamu.company.email;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import shamu.company.utils.EmailUtil;

@Service
public class EmailService {

  private final EmailRepository emailRepository;

  private final TaskScheduler taskScheduler;

  private final EmailUtil emailUtil;

  @Value("${email.retryLimit}")
  Integer emailRetryLimit;

  @Autowired
  public EmailService(final EmailRepository emailRepository, final TaskScheduler taskScheduler,
      final EmailUtil emailUtil) {
    this.emailRepository = emailRepository;
    this.taskScheduler = taskScheduler;
    this.emailUtil = emailUtil;
  }

  public Email save(final Email email) {
    return emailRepository.save(email);
  }

  public List<Email> findAllUnfinishedTasks() {
    return emailRepository.findAllUnfinishedTasks(emailRetryLimit);
  }

  public void scheduleEmail(final Email email) {
    Timestamp sendDate = email.getSendDate();
    if (sendDate == null) {
      sendDate = new Timestamp(new Date().getTime());
    }
    taskScheduler.schedule(getEmailTask(email), sendDate);
  }

  public void saveAndScheduleEmail(final Email email) {
    save(email);
    scheduleEmail(email);
  }

  private void reScheduleFailedEmail(final Email email) {
    final Integer currentRetryCount = email.getRetryCount() == null ? 0 : email.getRetryCount() + 1;
    email.setRetryCount(currentRetryCount);
    save(email);

    if (email.getRetryCount() >= emailRetryLimit) {
      return;
    }

    final Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.HOUR, 1);
    email.setSendDate(new Timestamp(calendar.getTimeInMillis()));
    saveAndScheduleEmail(email);
  }

  public Runnable getEmailTask(final Email email) {
    return () -> {
      try {
        emailUtil.send(email);
        email.setSentAt(new Timestamp(new Date().getTime()));
        emailRepository.save(email);
      } catch (final Exception exception) {
        reScheduleFailedEmail(email);
      }
    };
  }
}
