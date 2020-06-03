package shamu.company;

import static org.mockito.BDDMockito.given;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.authorization.MethodPermissionEvaluator;
import shamu.company.authorization.Permission;
import shamu.company.authorization.Permission.Name;
import shamu.company.authorization.Permission.PermissionType;
import shamu.company.authorization.PermissionUtils;
import shamu.company.authorization.UserPermissionUtils;
import shamu.company.benefit.repository.BenefitCoveragesRepository;
import shamu.company.benefit.service.BenefitPlanDependentService;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.benefit.service.BenefitPlanTypeService;
import shamu.company.common.config.DefaultAuthenticationEntryPoint;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.info.entity.mapper.UserEmergencyContactMapper;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.service.JobService;
import shamu.company.job.service.JobUserService;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.dto.AuthUser;
import shamu.company.server.service.CompanyEmailService;
import shamu.company.server.service.CompanyUserService;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.timeoff.service.CompanyPaidHolidayService;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.timeoff.service.TimeOffPolicyUserService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;
import shamu.company.utils.UuidUtil;

@Import({
  DefaultAuthenticationEntryPoint.class,
  BaseRestControllerConfiguration.class,
  MethodPermissionEvaluator.class,
  PermissionUtils.class,
  UserPermissionUtils.class
})
public class WebControllerBaseTests {

  @MockBean protected AuthUserCacheManager authUserCacheManager;
  @MockBean protected UserService userService;
  @MockBean protected CompanyService companyService;
  @MockBean protected CompanyUserService companyUserService;
  @MockBean protected CompanyEmailService companyEmailService;
  @MockBean protected TimeOffRequestService timeOffRequestService;
  @MockBean protected UserAddressService userAddressService;
  @MockBean protected BenefitPlanService benefitPlanService;
  @MockBean protected BenefitPlanDependentService benefitPlanDependentService;
  @MockBean protected TimeOffPolicyUserService timeOffPolicyUserService;
  @MockBean protected TimeOffPolicyService timeOffPolicyService;
  @MockBean protected PaidHolidayService paidHolidayService;
  @MockBean protected CompanyPaidHolidayService companyPaidHolidayService;
  @MockBean protected JobService jobService;
  @MockBean protected UserEmergencyContactService userEmergencyContactService;
  @MockBean protected BenefitPlanTypeService benefitPlanTypeService;
  @MockBean protected JobUserService jobUserService;
  @MockBean protected UserEmergencyContactMapper userEmergencyContactMapper;
  @MockBean protected BenefitCoveragesRepository benefitCoveragesRepository;
  @MockBean protected Auth0Helper auth0Helper;
  @MockBean protected AttendanceSetUpService attendanceSetUpService;

  protected HttpHeaders httpHeaders;

  protected Company company;

  protected Company theOtherCompany = new Company(UuidUtil.getUuidString());

  protected AuthUser currentUser;

  protected User targetUser;

  protected AuthUser getAuthUser() {
    final DefaultJwtAuthenticationToken authenticationToken =
        (DefaultJwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    return authenticationToken.getAuthUser();
  }

  protected void setPermission(final String permission) {
    getAuthUser().setPermissions(Collections.singletonList(permission));
  }

  protected void buildAuthUserAsDeactivatedUser() {
    final List<String> permissions = getDeactivateUserPermissions();
    getAuthUser().setPermissions(permissions);
    getAuthUser().setRole(Role.INACTIVATE);
  }

  protected void buildAuthUserAsEmployee() {
    final List<String> permissions = getEmployeePermissions();
    getAuthUser().setPermissions(permissions);
    getAuthUser().setRole(Role.EMPLOYEE);
  }

  protected void buildAuthUserAsManager() {
    final List<String> permissions = getManagerPermissions();
    getAuthUser().setPermissions(permissions);
    getAuthUser().setRole(Role.MANAGER);
  }

  protected void buildAuthUserAsAdmin() {
    final List<String> permissions = getAdminPermissions();
    getAuthUser().setPermissions(permissions);
    getAuthUser().setRole(Role.ADMIN);
  }

  private List<String> getAdminPermissions() {
    final List<String> permissions =
        Arrays.stream(Name.values())
            .filter(name -> PermissionType.ADMIN_PERMISSION.equals(name.getPermissionType()))
            .map(Name::name)
            .collect(Collectors.toList());
    permissions.addAll(getManagerPermissions());
    return permissions;
  }

  private List<String> getManagerPermissions() {
    final List<String> permissions =
        Arrays.stream(Name.values())
            .filter(name -> PermissionType.MANAGER_PERMISSION.equals(name.getPermissionType()))
            .map(Name::name)
            .collect(Collectors.toList());
    permissions.addAll(getEmployeePermissions());
    return permissions;
  }

  private List<String> getEmployeePermissions() {
    final List<String> permissions =
        Arrays.stream(Name.values())
            .filter(name -> PermissionType.EMPLOYEE_PERMISSION.equals(name.getPermissionType()))
            .map(Name::name)
            .collect(Collectors.toList());
    permissions.addAll(getSelfPermissions());
    return permissions;
  }

  private List<String> getSelfPermissions() {
    return Arrays.stream(Name.values())
        .filter(name -> PermissionType.SELF_PERMISSION.equals(name.getPermissionType()))
        .map(Name::name)
        .collect(Collectors.toList());
  }

  private List<String> getDeactivateUserPermissions() {
    final List<String> permissions = new ArrayList<>();
    permissions.add(Permission.Name.EDIT_SELF.name());
    permissions.add(Permission.Name.MANAGE_SELF_TIME_OFF_BALANCE.name());
    permissions.add(Permission.Name.VIEW_SELF.name());
    permissions.add(Permission.Name.VIEW_USER_ROLE_AND_STATUS.name());
    permissions.add(Permission.Name.VIEW_SELF_BENEFITS.name());
    return permissions;
  }

  @BeforeEach
  void setUp() throws NoSuchAlgorithmException {
    final AuthUser authUser = new AuthUser();
    authUser.setId(UuidUtil.getUuidString());
    authUser.setCompanyId(UuidUtil.getUuidString());
    authUser.setEmail("example@example.com");
    authUser.setUserId(authUser.getId());
    authUser.setPermissions(Collections.emptyList());
    final DefaultJwtAuthenticationToken defaultJwtAuthenticationToken =
        new DefaultJwtAuthenticationToken(
            JwtUtil.getJwt(), authUser.getId(), Collections.emptyList(), authUser);
    SecurityContextHolder.getContext().setAuthentication(defaultJwtAuthenticationToken);

    httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    company = new Company(getAuthUser().getCompanyId());
    currentUser = getAuthUser();
    targetUser = new User();

    given(authUserCacheManager.getCachedUser(Mockito.any())).willReturn(getAuthUser());
  }
}
