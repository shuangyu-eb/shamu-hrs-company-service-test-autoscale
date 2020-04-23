package shamu.company.email.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.email.entity.Email;

public interface EmailRepository extends BaseRepository<Email, String> {

  @Query(value = "select e from Email e where e.sentAt is null and e.retryCount < ?1")
  List<Email> findAllUnfinishedTasks(Integer emailRetryLimit);

  Email findFirstByToAndSubjectOrderBySendDateDesc(String to, String subject);

  Email findByMessageId(String messageId);
}
