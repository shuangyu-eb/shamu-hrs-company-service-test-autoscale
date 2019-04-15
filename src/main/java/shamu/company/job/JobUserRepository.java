package shamu.company.job;

import java.util.List;
import org.springframework.stereotype.Repository;
import shamu.company.common.BaseRepository;
import shamu.company.user.entity.User;

@Repository
public interface JobUserRepository extends BaseRepository<JobUser, Long> {

  List<JobUser> findAllByUserIn(List<User> users);
}
