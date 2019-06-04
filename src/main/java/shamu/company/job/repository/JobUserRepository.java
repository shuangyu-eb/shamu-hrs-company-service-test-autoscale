package shamu.company.job.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import shamu.company.common.repository.BaseRepository;
import shamu.company.job.entity.JobUser;
import shamu.company.user.entity.User;

@Repository
public interface JobUserRepository extends BaseRepository<JobUser, Long> {

  @Query(value = "SELECT * FROM jobs_users WHERE user_id=?1 AND deleted_at IS NULL ",
      nativeQuery = true)
  JobUser findByUserId(Long userId);

  JobUser findJobUserByUser(User user);
}
