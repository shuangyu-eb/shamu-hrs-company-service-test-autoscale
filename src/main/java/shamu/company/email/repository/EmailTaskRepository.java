package shamu.company.email.repository;

import java.util.List;
import shamu.company.email.entity.Email;

public interface EmailTaskRepository {

  List<Email> findAllUnfinishedTasks(String schema, Integer emailRetryLimit);
}
