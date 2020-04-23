package shamu.company.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.user.entity.User;
import shamu.company.user.repository.UserRolesRepository;
import shamu.company.user.service.UserRoleService;

public class UserRoleServiceTest {
  @Mock private UserRolesRepository userRolesRepository;

  @InjectMocks private UserRoleService userRoleService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenFindAll_thenShouldCall() {
    userRoleService.findAll();
    Mockito.verify(userRolesRepository, Mockito.times(1)).findAll();
  }

  @Nested
  class GetRole {
    @Test
    void whenGetAdmin_thenShouldCall() {
      userRoleService.getAdmin();
      Mockito.verify(userRolesRepository, Mockito.times(1)).findByName(User.Role.ADMIN.getValue());
    }

    @Test
    void whenGetManager_thenShouldCall() {
      userRoleService.getManager();
      Mockito.verify(userRolesRepository, Mockito.times(1))
          .findByName(User.Role.MANAGER.getValue());
    }

    @Test
    void whenGetEmployee_thenShouldCall() {
      userRoleService.getEmployee();
      Mockito.verify(userRolesRepository, Mockito.times(1))
          .findByName(User.Role.EMPLOYEE.getValue());
    }

    @Test
    void whenGetInactive_thenShouldCall() {
      userRoleService.getInactive();
      Mockito.verify(userRolesRepository, Mockito.times(1))
          .findByName(User.Role.INACTIVATE.getValue());
    }
  }
}
