package shamu.company.user.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.UserStatus;

public interface UserStatusRepository extends BaseRepository<UserStatus, String> {

  UserStatus findByName(String userStatus);

  List<UserStatus> findAll();
}
