package shamu.company.email.repository;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import shamu.company.email.entity.Email;
import shamu.company.helpers.DatabaseSessionHelper;

@Repository
public class EmailTaskRepositoryImpl implements EmailTaskRepository {

  private final DatabaseSessionHelper sessionHelper;

  public EmailTaskRepositoryImpl(final DatabaseSessionHelper sessionHelper) {
    this.sessionHelper = sessionHelper;
  }

  @Override
  public List<Email> findAllUnfinishedTasks(final String schema, final Integer emailRetryLimit) {
    try (final Session session = sessionHelper.getSessionBySchema(schema)) {
      final Query<Email> query =
          session.createNativeQuery(
              "select * from emails e where e.sent_at is null and e.retry_count < ?1", Email.class);
      query.setParameter(1, emailRetryLimit);
      return query.getResultList();
    }
  }
}
