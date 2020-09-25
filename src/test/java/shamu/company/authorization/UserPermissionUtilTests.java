package shamu.company.authorization;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.RandomStringUtils;
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
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.entity.Job;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.timeoff.service.TimeOffPolicyUserService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;
import shamu.company.utils.UuidUtil;

class UserPermissionUtilTests {

  @Mock private UserService userService;
  @Mock private CompanyService companyService;
  @Mock private TimeOffRequestService timeOffRequestService;
  @Mock private UserAddressService userAddressService;
  @Mock private BenefitPlanService benefitPlanService;
  @Mock private BenefitPlanDependentService benefitPlanDependentService;
  @Mock private TimeOffPolicyUserService timeOffPolicyUserService;
  @Mock private TimeOffPolicyService timeOffPolicyService;
  @Mock private PaidHolidayService paidHolidayService;
  @Mock private AuthUserCacheManager cacheManager;
  @Mock private UserEmergencyContactService userEmergencyContactService;
  @InjectMocks private UserPermissionUtils userPermissionUtils;

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

    final Authentication authentication =
        new DefaultJwtAuthenticationToken(
            jwt, RandomStringUtils.randomAlphabetic(16), Collections.emptyList(), authUser, "test@gmail.com");
    SecurityContextHolder.getContext().setAuthentication(authentication);

    Whitebox.setInternalState(userPermissionUtils, "authUserCacheManager", cacheManager);
    Mockito.when(cacheManager.getCachedUser(Mockito.anyString())).thenReturn(authUser);
  }

  Authentication initAuthenticationWithPermission(final List<String> permissionNames) {
    final List<GrantedAuthority> authorities =
        permissionNames.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    final Authentication authentication =
        new DefaultJwtAuthenticationToken(
            jwt, RandomStringUtils.randomAlphabetic(16), authorities, authUser, "");
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return SecurityContextHolder.getContext().getAuthentication();
  }

  Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @Nested
  class HasPermissionOfDepartment {

    @Test
    void whenHasDepartmentPermission_thenShouldReturnTrue() {

      final Department department = new Department();

      Mockito.when(companyService.findDepartmentsById(Mockito.anyString())).thenReturn(department);

      final Name permission = Name.CREATE_DEPARTMENT;
      final Type permissionType = Type.DEPARTMENT;
      initAuthenticationWithPermission(Collections.singletonList(permission.name()));
      assertThat(
              userPermissionUtils.hasPermission(
                  getAuthentication(),
                  RandomStringUtils.randomAlphabetic(16),
                  permissionType,
                  permission))
          .isTrue();
    }

    @Test
    void whenNoDepartmentPermission_thenShouldReturnFalse() {

      final Department department = new Department();

      Mockito.when(companyService.findDepartmentsById(Mockito.anyString())).thenReturn(department);

      final Name permission = Name.CREATE_DEPARTMENT;
      final Type permissionType = Type.DEPARTMENT;
      assertThat(
              userPermissionUtils.hasPermission(
                  getAuthentication(),
                  RandomStringUtils.randomAlphabetic(16),
                  permissionType,
                  permission))
          .isFalse();
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

      private Authentication authentication;

      @BeforeEach
      void init() {
        permission = Name.CREATE_AND_APPROVED_TIME_OFF_REQUEST;
        authentication =
            initAuthenticationWithPermission(Collections.singletonList(permission.name()));
      }

      @Test
      void asSelf_whenNoManager_thenShouldSuccess() {
        final User targetUser = new User(authUser.getId());
        Mockito.when(userService.findById(targetUser.getId())).thenReturn(targetUser);
        assertThat(
                userPermissionUtils.hasPermission(
                    authentication, targetUser.getId(), type, permission))
            .isTrue();
      }

      @Test
      void notSelf_asAdmin_whenSameCompany_thenShouldSuccess() {
        authUser.setRole(Role.ADMIN);
        final User targetUser = new User(UuidUtil.getUuidString());
        Mockito.when(userService.findById(targetUser.getId())).thenReturn(targetUser);
        assertThat(
                userPermissionUtils.hasPermission(
                    authentication, targetUser.getId(), type, permission))
            .isTrue();
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

        final Authentication authentication =
            initAuthenticationWithPermission(
                Collections.singletonList(Name.MANAGE_SELF_TIME_OFF_REQUEST.name()));

        assertThat(
                userPermissionUtils.hasPermission(
                    authentication,
                    RandomStringUtils.randomAlphabetic(16),
                    type,
                    Name.MANAGE_SELF_TIME_OFF_REQUEST))
            .isTrue();
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

      final Authentication authentication =
          initAuthenticationWithPermission(
              Collections.singletonList(Name.MANAGE_SELF_TIME_OFF_REQUEST.name()));
      assertThat(
              userPermissionUtils.hasPermission(
                  authentication,
                  RandomStringUtils.randomAlphabetic(16),
                  Type.TIME_OFF_POLICY_USER,
                  Name.MANAGE_SELF_TIME_OFF_REQUEST))
          .isTrue();
    }

    @Test
    void whenNoPermission_thenReturnFalse() {
      final User targetUser = new User();
      targetUser.setId(RandomStringUtils.randomAlphabetic(16));
      final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
      timeOffPolicyUser.setUser(targetUser);
      Mockito.when(timeOffPolicyUserService.findById(Mockito.anyString()))
          .thenReturn(timeOffPolicyUser);
      assertThat(
              userPermissionUtils.hasPermission(
                  getAuthentication(),
                  RandomStringUtils.randomAlphabetic(16),
                  Type.TIME_OFF_POLICY_USER,
                  Name.MANAGE_SELF_TIME_OFF_REQUEST))
          .isFalse();
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

      Mockito.when(userService.findById(Mockito.anyString())).thenReturn(targetUser);
    }

    @Nested
    class IsSelfPermission {

      @Test
      void whenIsCurrentUser_thenReturnTrue() {

        final User currentUser = new User();
        currentUser.setId(authUser.getId());
        Mockito.when(userService.findById(Mockito.anyString())).thenReturn(currentUser);
        final Authentication authentication =
            initAuthenticationWithPermission(Collections.singletonList(Name.EDIT_SELF.name()));
        assertThat(
                userPermissionUtils.hasPermission(
                    authentication,
                    RandomStringUtils.randomAlphabetic(16),
                    Type.USER,
                    Name.EDIT_SELF))
            .isTrue();
      }

      @Test
      void whenIsNotCurrentUser_thenReturnFalse() {
        assertThat(
                userPermissionUtils.hasPermission(
                    getAuthentication(),
                    RandomStringUtils.randomAlphabetic(16),
                    Type.USER,
                    Name.EDIT_SELF))
            .isFalse();
      }
    }

    @Nested
    class IsNotManagerPermission {

      @Test
      void whenHasPermission_thenShouldReturnTrue() {
        initAuthenticationWithPermission(Collections.singletonList(Name.CREATE_USER.name()));
        assertThat(
                userPermissionUtils.hasPermission(
                    getAuthentication(),
                    RandomStringUtils.randomAlphabetic(16),
                    Type.USER,
                    Name.CREATE_USER))
            .isTrue();
      }

      @Test
      void whenNoPermission_thenShouldReturnFalse() {
        assertThat(
                userPermissionUtils.hasPermission(
                    getAuthentication(),
                    RandomStringUtils.randomAlphabetic(16),
                    Type.USER,
                    Name.CREATE_USER))
            .isFalse();
      }
    }

    @Nested
    class IsAdminPermission {

      @Test
      void whenHasPermission_thenShouldReturnTrue() {
        initAuthenticationWithPermission(Collections.singletonList(Name.CREATE_DEPARTMENT.name()));
        assertThat(
                userPermissionUtils.hasPermission(
                    getAuthentication(),
                    RandomStringUtils.randomAlphabetic(16),
                    Type.USER,
                    Name.CREATE_DEPARTMENT))
            .isTrue();
      }

      @Test
      void whenNoPermission_thenShouldReturnFalse() {
        assertThat(
                userPermissionUtils.hasPermission(
                    getAuthentication(),
                    RandomStringUtils.randomAlphabetic(16),
                    Type.USER,
                    Name.CREATE_DEPARTMENT))
            .isFalse();
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

        assertThat(
                userPermissionUtils.hasPermission(
                    getAuthentication(),
                    RandomStringUtils.randomAlphabetic(16),
                    Type.USER,
                    Name.VIEW_USER_EMERGENCY_CONTACT))
            .isFalse();
      }

      @Test
      void whenIsTargetUserManager_thenReturnTrue() {

        Mockito.when(userService.getManagerUserIdById(Mockito.anyString()))
            .thenReturn(authUser.getId());
        initAuthenticationWithPermission(
            Collections.singletonList(Name.VIEW_USER_EMERGENCY_CONTACT.name()));

        assertThat(
                userPermissionUtils.hasPermission(
                    getAuthentication(),
                    RandomStringUtils.randomAlphabetic(16),
                    Type.USER,
                    Name.VIEW_USER_EMERGENCY_CONTACT))
            .isTrue();
      }
    }
  }

  @Nested
  class HasPermissionOfJobTitle {

    @Test
    void whenHasJobTitlePermission_thenShouldReturnTrue() {

      final Job job = new Job();
      Mockito.when(companyService.findJobsById(Mockito.anyString())).thenReturn(job);
      final Name permission = Name.CREATE_JOB;
      final Type permissionType = Type.JOB_TITLE;
      initAuthenticationWithPermission(Collections.singletonList(permission.name()));
      assertThat(
              userPermissionUtils.hasPermission(
                  getAuthentication(),
                  RandomStringUtils.randomAlphabetic(16),
                  permissionType,
                  permission))
          .isTrue();
    }

    @Test
    void whenNoJobTitlePermission_thenShouldReturnFalse() {

      final Job job = new Job();
      Mockito.when(companyService.findJobsById(Mockito.anyString())).thenReturn(job);
      final Name permission = Name.CREATE_JOB;
      final Type permissionType = Type.JOB_TITLE;
      assertThat(
              userPermissionUtils.hasPermission(
                  getAuthentication(),
                  RandomStringUtils.randomAlphabetic(16),
                  permissionType,
                  permission))
          .isFalse();
    }
  }

  @Nested
  class HasPermissionOfUserEmergencyContact {

    @Test
    void whenHasUserEmergencyContactPermission_thenShouldReturnTrue() {

      final UserEmergencyContact userEmergencyContact = new UserEmergencyContact();
      final User targetUser = new User();
      userEmergencyContact.setUser(targetUser);
      Mockito.when(userEmergencyContactService.findById(Mockito.anyString()))
          .thenReturn(userEmergencyContact);

      final Name permission = Name.VIEW_USER_EMERGENCY_CONTACT;
      final Type permissionType = Type.USER_EMERGENCY_CONTACT;
      initAuthenticationWithPermission(Collections.singletonList(permission.name()));
      assertThat(
              userPermissionUtils.hasPermission(
                  getAuthentication(),
                  RandomStringUtils.randomAlphabetic(16),
                  permissionType,
                  permission))
          .isTrue();
    }

    @Test
    void whenNoUserEmergencyContactPermission_thenShouldReturnFalse() {

      final UserEmergencyContact userEmergencyContact = new UserEmergencyContact();
      final User targetUser = new User();
      userEmergencyContact.setUser(targetUser);
      Mockito.when(userEmergencyContactService.findById(Mockito.anyString()))
          .thenReturn(userEmergencyContact);

      final Name permission = Name.VIEW_USER_EMERGENCY_CONTACT;
      final Type permissionType = Type.USER_EMERGENCY_CONTACT;
      assertThat(
              userPermissionUtils.hasPermission(
                  getAuthentication(),
                  RandomStringUtils.randomAlphabetic(16),
                  permissionType,
                  permission))
          .isFalse();
    }
  }

  @Nested
  class IsCurrentUserId {

    @Test
    void whenIsCurrentUserId_thenReturnTrue() {
      assertThat(userPermissionUtils.isCurrentUserId(authUser.getId())).isTrue();
    }

    @Test
    void whenIsNotCurrentUserId_thenReturnFalse() {
      assertThat(userPermissionUtils.isCurrentUserId(RandomStringUtils.randomAlphabetic(16)))
          .isFalse();
    }
  }

  @Nested
  class IsCurrentUserEmail {

    @Test
    void whenIsCurrentUserEmail_thenReturnTrue() {
      assertThat(userPermissionUtils.isCurrentUserEmail(authUser.getEmail())).isTrue();
    }

    @Test
    void whenIsNotCurrentUserEmail_thenReturnFalse() {
      assertThat(userPermissionUtils.isCurrentUserEmail("exampleb@gmail.com")).isFalse();
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
      assertThat(userPermissionUtils.hasAuthority(Name.CREATE_DEPARTMENT.name())).isTrue();
    }

    @Test
    void whenNoAuthority_thenReturnFalse() {
      assertThat(userPermissionUtils.hasAuthority(Name.VIEW_USER_EMERGENCY_CONTACT.name()))
          .isFalse();
    }
  }
}
