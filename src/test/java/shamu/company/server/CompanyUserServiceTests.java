package shamu.company.server;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.authorization.Permission;
import shamu.company.authorization.PermissionUtils;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.server.dto.AuthUser;
import shamu.company.server.service.CompanyUserService;
import shamu.company.user.service.UserService;

public class CompanyUserServiceTests {

  @Mock private CompanyService companyService;

  @Mock private UserService userService;

  @Mock private PermissionUtils permissionUtils;

  @InjectMocks private CompanyUserService companyUserService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void testFindAllById() {
    final List<String> ids = new ArrayList<>();
    Assertions.assertDoesNotThrow(() -> companyUserService.findAllById(ids));
  }

  @Test
  void testFindUserById() {
    Assertions.assertDoesNotThrow(() -> companyUserService.findUserById("1"));
  }

  @Test
  void testFindAllUsers() {
    Mockito.when(companyService.findById(Mockito.anyString())).thenReturn(new Company());
    Assertions.assertDoesNotThrow(() -> companyUserService.findAllUsers("1"));
  }

  @Test
  void testfindUserByUserId() {
    Assertions.assertDoesNotThrow(() -> companyUserService.findUserByUserId("1"));
  }

  @Nested
  class FindAllEmployees {
    final AuthUser authUser = new AuthUser();
    final EmployeeListSearchCondition condition = new EmployeeListSearchCondition();

    @Test
    void whenIsAdmin_thenShouldIncludeDeactivated() {
      Mockito.when(permissionUtils.hasAuthority(Permission.Name.VIEW_DISABLED_USER.name()))
          .thenReturn(true);
      companyUserService.findAllEmployees(authUser, condition);
      Assertions.assertEquals(true, condition.getIncludeDeactivated());
    }

    @Test
    void whenIsNotAdmin_thenShouldNotIncludeDeactivated() {
      Mockito.when(permissionUtils.hasAuthority(Permission.Name.VIEW_DISABLED_USER.name()))
          .thenReturn(false);
      companyUserService.findAllEmployees(authUser, condition);
      Assertions.assertEquals(false, condition.getIncludeDeactivated());
    }
  }
}
