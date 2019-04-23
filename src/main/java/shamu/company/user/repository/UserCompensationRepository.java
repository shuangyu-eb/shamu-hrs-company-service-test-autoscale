package shamu.company.user.repository;

import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;

public interface UserCompensationRepository extends BaseRepository<UserCompensation, Long> {

  UserCompensation findByUser(User user);
}
