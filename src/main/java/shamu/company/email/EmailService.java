package shamu.company.email;

import java.util.List;

public interface EmailService {

  List<Email> findAllUnfinishedTasks();

  void scheduleEmail(Email email);

  Runnable getEmailTask(Email email);

  Email save(Email email);

  void saveAndScheduleEmail(Email email);
}
