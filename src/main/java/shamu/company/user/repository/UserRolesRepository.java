package shamu.company.user.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.UserRole;

public interface UserRolesRepository extends BaseRepository<UserRole, String> {

  UserRole findByName(String userStatus);

  List<UserRole> findAll();
}
