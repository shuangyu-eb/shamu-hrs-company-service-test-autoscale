package shamu.company.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.UserRole;

public interface UserRoleRepository extends BaseRepository<UserRole, Long> {

  UserRole findByName(String name);
}
