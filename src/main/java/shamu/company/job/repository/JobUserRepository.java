package shamu.company.job.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import shamu.company.common.repository.BaseRepository;
import shamu.company.job.entity.JobUser;
import shamu.company.user.entity.User;

@Repository
public interface JobUserRepository extends BaseRepository<JobUser, String> {

  @Query(value = "SELECT * FROM jobs_users WHERE user_id=unhex(?1)",
      nativeQuery = true)
  JobUser findByUserId(String userId);

  JobUser findJobUserByUser(User user);

  @Query(
          value = "SELECT count(1) FROM jobs_users ju"
                  + " WHERE ju.job_id = unhex(?1) ",
          nativeQuery = true)
  Integer getCountByJobId(String jobId);
}
