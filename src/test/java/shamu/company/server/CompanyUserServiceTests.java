package shamu.company.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;
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
import shamu.company.server.dto.CompanyDto;
import shamu.company.server.dto.CompanyDtoProjection;
import shamu.company.server.dto.CompanyUser;
import shamu.company.server.service.CompanyUserService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserService;

class CompanyUserServiceTests {

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

  @Test
  void testFindCompanyDtoByUserId() {
    final String userId = RandomStringUtils.randomAlphabetic(16);
    final CompanyDtoProjection companyDto = new CompanyDtoProjection() {
      @Override
      public String getId() {
        return userId;
      }

      @Override
      public String getName() {
        return null;
      }
    };
    Mockito.when(companyService.findCompanyDtoByUserId(userId)).thenReturn(companyDto);

    final CompanyDtoProjection result = companyUserService.findCompanyDtoByUserId(userId);
    Assertions.assertEquals(companyDto.getId(), result.getId());
  }

  @Test
  void testFindCompaniesByIds() {
    final String companyId = RandomStringUtils.randomAlphabetic(16);
    final List<String> parameterIds = Collections.singletonList(companyId);
    final List<Company> companies = new ArrayList<>();
    final Company company = new Company();
    company.setId(companyId);
    companies.add(company);

    Mockito.when(companyService.findAllById(parameterIds)).thenReturn(companies);

    final List<CompanyDto> result = companyUserService.findCompaniesByIds(parameterIds);
    Assertions.assertFalse(result.isEmpty());
    Assertions.assertEquals(companyId, result.get(0).getId());
  }

  @Test
  void testFindSuperUser() {
    final String companyId = RandomStringUtils.randomAlphabetic(16);
    final User user = new User();
    user.setId(RandomStringUtils.randomAlphabetic(16));
    user.setUserPersonalInformation(new UserPersonalInformation());
    user.getUserPersonalInformation().setFirstName("first");
    user.getUserPersonalInformation().setLastName("last");
    user.setUserContactInformation(new UserContactInformation());
    user.getUserContactInformation().setEmailWork("email@example.com");

    Mockito.when(userService.findSuperUser(companyId)).thenReturn(user);

    final CompanyUser resultUser = companyUserService.findSuperUser(companyId);
    assertThat(resultUser.getId()).isEqualTo(user.getId());
  }

  @Test
  void testFindAllRegisteredUsers() {
    final Company company = new Company();
    company.setId("1");
    Mockito.when(companyService.findById(Mockito.anyString())).thenReturn(company);
    Mockito.when(userService.findRegisteredUsersByCompany(Mockito.anyString())).thenReturn(Collections.emptyList());
    final List<User> users = companyUserService.findAllRegisteredUsers("1");
    assertThat(users).isEmpty();
  }
}
