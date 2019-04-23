package shamu.company.job.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import shamu.company.common.repository.BaseRepository;
import shamu.company.job.entity.JobUser;
import shamu.company.user.entity.User;

@Repository
public interface JobUserRepository extends BaseRepository<JobUser, Long> {

  List<JobUser> findAllByUserIn(List<User> users);

  JobUser findJobUserByUser(User user);
}
