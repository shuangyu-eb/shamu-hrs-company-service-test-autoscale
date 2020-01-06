package shamu.company.user.service;

import java.util.List;
import org.springframework.stereotype.Service;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserRole;
import shamu.company.user.repository.UserRolesRepository;

@Service
public class UserRoleService {

  private final UserRolesRepository userRolesRepository;

  public UserRoleService(final UserRolesRepository userRolesRepository) {
    this.userRolesRepository = userRolesRepository;
  }

  public UserRole getAdmin() {
    return userRolesRepository.findByName(Role.ADMIN.getValue());
  }

  public UserRole getManager() {
    return userRolesRepository.findByName(Role.MANAGER.getValue());
  }

  public UserRole getEmployee() {
    return userRolesRepository.findByName(Role.EMPLOYEE.getValue());
  }

  public UserRole getInactive() {
    return userRolesRepository.findByName(Role.INACTIVATE.getValue());
  }

  public List<UserRole> findAll() {
    return userRolesRepository.findAll();
  }
}
