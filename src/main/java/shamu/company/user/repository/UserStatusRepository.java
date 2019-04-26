package shamu.company.user.repository;

import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.UserStatus;

public interface UserStatusRepository extends BaseRepository<UserStatus, Long> {

  UserStatus findByName(String userStatus);
}
