package shamu.company.job;

import org.springframework.stereotype.Repository;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.User;

import java.util.List;

@Repository
public interface JobUserRepository extends BaseRepository<JobUser, Long> {

    List<JobUser> findAllByUserIn(List<User> users);
}
