package shamu.company;

import static org.mockito.BDDMockito.given;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import shamu.company.authorization.MethodPermissionEvaluator;
import shamu.company.authorization.PermissionUtils;
import shamu.company.authorization.UserPermissionUtils;
import shamu.company.benefit.service.BenefitPlanDependentService;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.benefit.service.BenefitPlanTypeService;
import shamu.company.common.config.DefaultAuthenticationEntryPoint;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.company.service.CompanyService;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.service.JobService;
import shamu.company.job.service.JobUserService;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.timeoff.service.CompanyPaidHolidayService;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.timeoff.service.TimeOffPolicyUserService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;
import shamu.company.utils.UuidUtil;

@Import({DefaultAuthenticationEntryPoint.class, BaseRestControllerConfiguration.class, MethodPermissionEvaluator.class,
    PermissionUtils.class, UserPermissionUtils.class})
public class WebControllerBaseTests {

  @MockBean
  protected AuthUserCacheManager authUserCacheManager;
  @MockBean
  protected UserService userService;
  @MockBean
  protected CompanyService companyService;
  @MockBean
  protected TimeOffRequestService timeOffRequestService;
  @MockBean
  protected UserAddressService userAddressService;
  @MockBean
  protected BenefitPlanService benefitPlanService;
  @MockBean
  protected BenefitPlanDependentService benefitPlanDependentService;
  @MockBean
  protected TimeOffPolicyUserService timeOffPolicyUserService;
  @MockBean
  protected TimeOffPolicyService timeOffPolicyService;
  @MockBean
  protected PaidHolidayService paidHolidayService;
  @MockBean
  protected CompanyPaidHolidayService companyPaidHolidayService;
  @MockBean
  protected JobService jobService;
  @MockBean
  protected UserEmergencyContactService userEmergencyContactService;
  @MockBean
  protected BenefitPlanTypeService benefitPlanTypeService;
  @MockBean
  protected JobUserService jobUserService;

  protected AuthUser getAuthUser() {
    final DefaultJwtAuthenticationToken authenticationToken =
        (DefaultJwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
     return authenticationToken.getAuthUser();
  }

  protected void setPermission(final String permission) {
    getAuthUser().setPermissions(Collections.singletonList(permission));
  }

  @BeforeEach
  void setUp() {
    final AuthUser authUser = new AuthUser();
    authUser.setId(UuidUtil.getUuidString());
    authUser.setCompanyId(UuidUtil.getUuidString());
    authUser.setEmail("example@example.com");
    authUser.setUserId(authUser.getId());
    authUser.setPermissions(Collections.emptyList());
    final DefaultJwtAuthenticationToken defaultJwtAuthenticationToken =
        new DefaultJwtAuthenticationToken(JwtUtil.getJwt(), authUser.getId(), Collections.emptyList(), authUser);
    SecurityContextHolder.getContext().setAuthentication(defaultJwtAuthenticationToken);

    given(authUserCacheManager.getCachedUser(Mockito.any())).willReturn(getAuthUser());
  }
}
