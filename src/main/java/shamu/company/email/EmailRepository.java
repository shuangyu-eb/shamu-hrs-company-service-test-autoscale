package shamu.company.email;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;

public interface EmailRepository extends BaseRepository<Email, Long> {

  @Query(
      value =
          "SELECT * FROM emails WHERE deleted_at IS NULL "
              + "AND sent_at IS NULL AND send_date <= now()",
      nativeQuery = true)
  List<Email> findAllUnfinishedTasks();
}
