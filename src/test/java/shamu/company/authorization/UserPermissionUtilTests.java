package shamu.company.authorization;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import shamu.company.authorization.Permission.Name;
import shamu.company.benefit.service.BenefitPlanDependentService;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.company.service.CompanyService;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.AuthUser;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.service.CompanyPaidHolidayService;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.timeoff.service.TimeOffPolicyUserService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;

class UserPermissionUtilTests {

  @Mock
  private UserService userService;
  @Mock
  private CompanyService companyService;
  @Mock
  private TimeOffRequestService timeOffRequestService;
  @Mock
  private UserAddressService userAddressService;
  @Mock
  private BenefitPlanService benefitPlanService;
  @Mock
  private BenefitPlanDependentService benefitPlanDependentService;
  @Mock
  private TimeOffPolicyUserService timeOffPolicyUserService;
  @Mock
  private TimeOffPolicyService timeOffPolicyService;
  @Mock
  private PaidHolidayService paidHolidayService;
  @Mock
  private CompanyPaidHolidayService companyPaidHolidayService;
  @Mock
  private AuthUserCacheManager cacheManager;
  @InjectMocks
  private UserPermissionUtils userPermissionUtils;

  private AuthUser authUser;
  private Company company;
  private Jwt jwt;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);

    authUser = new AuthUser();
    authUser.setId(RandomStringUtils.randomAlphabetic(16));
    authUser.setCompanyId(RandomStringUtils.randomAlphabetic(16));
    authUser.setEmail("example@gmail.com");
    company = new Company();
    company.setId(authUser.getCompanyId());
    jwt = JwtUtil.getJwt();

    final Authentication authentication = new DefaultJwtAuthenticationToken(jwt,
        RandomStringUtils.randomAlphabetic(16), Collections.emptyList(), authUser);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    Whitebox.setInternalState(userPermissionUtils, "authUserCacheManager", cacheManager);
    Mockito.when(cacheManager.getCachedUser(Mockito.anyString())).thenReturn(authUser);
  }

  @Nested
  class HasPermissionOfDepartment {

    @Test
    void whenHasDepartmentPermission_thenShouldReturnTrue() {

      final Department department = new Department();
      department.setCompany(company);

      Mockito.when(companyService.getDepartmentsById(Mockito.anyString())).thenReturn(department);

      final Name permission = Name.CREATE_DEPARTMENT;
      final Type permissionType = Type.DEPARTMENT;
      initAuthenticationWithPermission(Collections.singletonList(permission.name()));
      final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
          RandomStringUtils.randomAlphabetic(16),
          permissionType,
          permission);
      Assertions.assertTrue(hasPermission);
    }

    @Test
    void whenNoDepartmentPermission_thenShouldReturnFalse() {

      final Department department = new Department();
      department.setCompany(company);

      Mockito.when(companyService.getDepartmentsById(Mockito.anyString())).thenReturn(department);

      final Name permission = Name.CREATE_DEPARTMENT;
      final Type permissionType = Type.DEPARTMENT;
      final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
          RandomStringUtils.randomAlphabetic(16),
          permissionType,
          permission);
      Assertions.assertFalse(hasPermission);
    }
  }

  @Nested
  class HasPermissionOfTimeOffRequest {

    private Type type;

    @BeforeEach
    void init() {
      type = Type.TIME_OFF_REQUEST;
    }

    @Nested
    class HasCreateAndApprovePermission {

      private Permission.Name permission;

      @BeforeEach
      void init() {
        permission = Name.CREATE_AND_APPROVED_TIME_OFF_REQUEST;
      }

      @Test
      void whenNoManager_thenShouldSuccess() {
        Mockito.when(userService.findById(Mockito.anyString())).thenReturn(new User());
        final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
            RandomStringUtils.randomAlphabetic(16),
            type,
            permission);
        Assertions.assertTrue(hasPermission);
      }

      @Test
      void whenIsManager_thenShouldSuccess() {
        final User targetUser = new User();
        final User managerUser = new User();
        managerUser.setId(authUser.getId());
        targetUser.setManagerUser(managerUser);

        Mockito.when(userService.findById(Mockito.anyString())).thenReturn(targetUser);
        final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
            RandomStringUtils.randomAlphabetic(16),
            type,
            permission);
        Assertions.assertTrue(hasPermission);
      }

      @Test
      void whenIsAdmin_thenShouldSuccess() {
        final User targetUser = new User();
        final User managerUser = new User();
        managerUser.setId(authUser.getId());
        targetUser.setManagerUser(managerUser);
        authUser.setRole(Role.ADMIN);

        Mockito.when(userService.findById(Mockito.anyString())).thenReturn(targetUser);
        final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
            RandomStringUtils.randomAlphabetic(16),
            type,
            permission);
        Assertions.assertTrue(hasPermission);
      }
    }

    @Nested
    class NotCreateAndApprovePermission {

      @Test
      void whenIsOtherPermission_thenDelegateToHasPermissionOfUser() {
        final TimeOffRequest mockedTimeOffRequest = new TimeOffRequest();
        final User targetUser = new User();
        targetUser.setId(authUser.getId());
        mockedTimeOffRequest.setRequesterUser(targetUser);

        Mockito.when(timeOffRequestService.getById(Mockito.anyString()))
            .thenReturn(mockedTimeOffRequest);

        final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
            RandomStringUtils.randomAlphabetic(16),
            type,
            Name.MANAGE_SELF_TIME_OFF_REQUEST);
        Assertions.assertTrue(hasPermission);
      }

    }
  }

  @Nested
  class HasPermissionOfTimeOffPolicyUser {

    @Test
    void whenHasPermission_thenReturnTrue() {
      final User targetUser = new User();
      targetUser.setId(authUser.getId());
      final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
      timeOffPolicyUser.setUser(targetUser);
      Mockito.when(timeOffPolicyUserService.findById(Mockito.anyString()))
          .thenReturn(timeOffPolicyUser);
      final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
          RandomStringUtils.randomAlphabetic(16),
          Type.TIME_OFF_POLICY_USER,
          Name.MANAGE_SELF_TIME_OFF_REQUEST);
      Assertions.assertTrue(hasPermission);
    }

    @Test
    void whenNoPermission_thenReturnFalse() {
      final User targetUser = new User();
      targetUser.setId(RandomStringUtils.randomAlphabetic(16));
      final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
      timeOffPolicyUser.setUser(targetUser);
      Mockito.when(timeOffPolicyUserService.findById(Mockito.anyString()))
          .thenReturn(timeOffPolicyUser);
      final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
          RandomStringUtils.randomAlphabetic(16),
          Type.TIME_OFF_POLICY_USER,
          Name.MANAGE_SELF_TIME_OFF_REQUEST);
      Assertions.assertFalse(hasPermission);
    }
  }

  @Nested
  class HasPermissionOfUser {

    User targetUser;

    @BeforeEach
    void init() {
      targetUser = new User();
      final Company targetCompany = new Company();
      targetCompany.setId(authUser.getCompanyId());
      targetUser.setId(RandomStringUtils.randomAlphabetic(16));
      targetUser.setCompany(targetCompany);

      Mockito.when(userService.findById(Mockito.anyString())).thenReturn(targetUser);
    }

    @Nested
    class IsSelfPermission {

      @Test
      void whenIsCurrentUser_thenReturnTrue() {

        final User currentUser = new User();
        currentUser.setId(authUser.getId());
        Mockito.when(userService.findById(Mockito.anyString())).thenReturn(currentUser);
        final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
            RandomStringUtils.randomAlphabetic(16),
            Type.USER,
            Name.EDIT_SELF);
        Assertions.assertTrue(hasPermission);

      }

      @Test
      void whenIsNotCurrentUser_thenReturnFalse() {
        final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
            RandomStringUtils.randomAlphabetic(16),
            Type.USER,
            Name.EDIT_SELF);
        Assertions.assertFalse(hasPermission);
      }
    }

    @Nested
    class IsNotManagerPermission {

      @Test
      void whenHasPermission_thenShouldReturnTrue() {
        initAuthenticationWithPermission(
            Collections.singletonList(Name.VIEW_DOCUMENT_REPORTS.name()));
        final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
            RandomStringUtils.randomAlphabetic(16),
            Type.USER,
            Name.VIEW_DOCUMENT_REPORTS);
        Assertions.assertTrue(hasPermission);
      }

      @Test
      void whenNoPermission_thenShouldReturnFalse() {
        final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
            RandomStringUtils.randomAlphabetic(16),
            Type.USER,
            Name.VIEW_DOCUMENT_REPORTS);
        Assertions.assertFalse(hasPermission);
      }
    }

    @Nested
    class IsAdminPermission {

      @Test
      void whenHasPermission_thenShouldReturnTrue() {
        initAuthenticationWithPermission(Collections.singletonList(Name.CREATE_DEPARTMENT.name()));
        final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
            RandomStringUtils.randomAlphabetic(16),
            Type.USER,
            Name.CREATE_DEPARTMENT);
        Assertions.assertTrue(hasPermission);
      }

      @Test
      void whenNoPermission_thenShouldReturnFalse() {
        final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
            RandomStringUtils.randomAlphabetic(16),
            Type.USER,
            Name.CREATE_DEPARTMENT);
        Assertions.assertFalse(hasPermission);
      }
    }

    @Nested
    class IsManagerPermission {

      @Test
      void whenIsNotTargetUserManager_thenReturnFalse() {

        Mockito.when(userService.getManagerUserIdById(Mockito.anyString()))
            .thenReturn(RandomStringUtils.randomAlphabetic(16));
        initAuthenticationWithPermission(
            Arrays.asList(Name.MANAGE_TEAM_USER.name(), Name.VIEW_USER_EMERGENCY_CONTACT.name()));

        final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
            RandomStringUtils.randomAlphabetic(16),
            Type.USER,
            Name.VIEW_USER_EMERGENCY_CONTACT);
        Assertions.assertFalse(hasPermission);
      }

      @Test
      void whenIsTargetUserManager_thenReturnTrue() {

        Mockito.when(userService.getManagerUserIdById(Mockito.anyString()))
            .thenReturn(authUser.getId());
        initAuthenticationWithPermission(
            Collections.singletonList(Name.VIEW_USER_EMERGENCY_CONTACT.name()));

        final boolean hasPermission = userPermissionUtils.hasPermission(getAuthentication(),
            RandomStringUtils.randomAlphabetic(16),
            Type.USER,
            Name.VIEW_USER_EMERGENCY_CONTACT);
        Assertions.assertTrue(hasPermission);
      }
    }
  }

  @Nested
  class IsCurrentUserId {

    @Test
    void whenIsCurrentUserId_thenReturnTrue() {
      final boolean isCurrentUser = userPermissionUtils.isCurrentUserId(authUser.getId());
      Assertions.assertTrue(isCurrentUser);
    }

    @Test
    void whenIsNotCurrentUserId_thenReturnFalse() {
      final boolean isCurrentUser = userPermissionUtils
          .isCurrentUserId(RandomStringUtils.randomAlphabetic(16));
      Assertions.assertFalse(isCurrentUser);
    }
  }

  @Nested
  class IsCurrentUserEmail {

    @Test
    void whenIsCurrentUserEmail_thenReturnTrue() {
      final boolean isCurrentUser = userPermissionUtils.isCurrentUserEmail(authUser.getEmail());
      Assertions.assertTrue(isCurrentUser);
    }

    @Test
    void whenIsNotCurrentUserEmail_thenReturnFalse() {
      final boolean isCurrentUser = userPermissionUtils
          .isCurrentUserEmail("exampleb@gmail.com");
      Assertions.assertFalse(isCurrentUser);
    }
  }

  @Nested
  class HasAuthority {

    @BeforeEach
    void init() {
      initAuthenticationWithPermission(Collections.singletonList(Name.CREATE_DEPARTMENT.name()));
    }

    @Test
    void whenHasAuthority_thenReturnTrue() {
      final boolean hasAuthority = userPermissionUtils.hasAuthority(Name.CREATE_DEPARTMENT.name());
      Assertions.assertTrue(hasAuthority);
    }

    @Test
    void whenNoAuthority_thenReturnFalse() {
      final boolean hasAuthority = userPermissionUtils
          .hasAuthority(Name.VIEW_USER_EMERGENCY_CONTACT.name());
      Assertions.assertFalse(hasAuthority);
    }
  }

  void initAuthenticationWithPermission(final List<String> permissionNames) {
    final List<GrantedAuthority> authorities = permissionNames.stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
    final Authentication authentication = new DefaultJwtAuthenticationToken(jwt,
        RandomStringUtils.randomAlphabetic(16), authorities, authUser);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }
}
