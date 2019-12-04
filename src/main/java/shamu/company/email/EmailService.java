package shamu.company.email;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import shamu.company.helpers.EmailHelper;

@Data
@Service
public class EmailService {

  private final EmailRepository emailRepository;

  private final TaskScheduler taskScheduler;

  private final EmailHelper emailHelper;

  private final Integer emailRetryLimit;

  @Autowired
  public EmailService(final EmailRepository emailRepository, final TaskScheduler taskScheduler,
      final EmailHelper emailHelper, @Value("${email.retryLimit}") final Integer emailRetryLimit) {
    this.emailRepository = emailRepository;
    this.taskScheduler = taskScheduler;
    this.emailHelper = emailHelper;
    this.emailRetryLimit = emailRetryLimit;
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
      sendDate = Timestamp.valueOf(LocalDateTime.now());
    }
    taskScheduler.schedule(getEmailTask(email), sendDate);
  }

  public void saveAndScheduleEmail(final Email email) {
    save(email);
    scheduleEmail(email);
  }

  private void rescheduleFailedEmail(final Email email) {
    final Integer currentRetryCount = email.getRetryCount() == null ? 0 : email.getRetryCount() + 1;
    email.setRetryCount(currentRetryCount);
    save(email);

    if (email.getRetryCount() >= emailRetryLimit) {
      return;
    }

    final LocalDateTime afterOneHour = LocalDateTime.now().plusHours(1);
    email.setSendDate(Timestamp.valueOf(afterOneHour));
    saveAndScheduleEmail(email);
  }

  public Runnable getEmailTask(final Email email) {
    return () -> {
      try {
        emailHelper.send(email);
        email.setSentAt(new Timestamp(new Date().getTime()));
        emailRepository.save(email);
      } catch (final Exception exception) {
        rescheduleFailedEmail(email);
      }
    };
  }
}
