package shamu.company.email;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;

public interface EmailRepository extends BaseRepository<Email, Long> {

  @Query(
      value =
          "select e from Email e where e.sentAt is null and e.retryCount < ?1")
  List<Email> findAllUnfinishedTasks(Integer emailRetryLimit);


  Email getFirstByToAndSubjectOrderBySendDateDesc(String to, String subject);

}
