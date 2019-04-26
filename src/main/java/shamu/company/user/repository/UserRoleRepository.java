package shamu.company.user.repository;

import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.UserRole;

public interface UserRoleRepository extends BaseRepository<UserRole, Long> {

  UserRole findByName(String name);
}
