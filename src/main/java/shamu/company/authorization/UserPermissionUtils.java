package shamu.company.authorization;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import shamu.company.authorization.Permission.Name;
import shamu.company.authorization.Permission.PermissionType;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.service.BenefitPlanDependentService;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.common.BaseAuthorityDto;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.company.service.CompanyService;
import shamu.company.timeoff.entity.CompanyPaidHoliday;
import shamu.company.timeoff.entity.PaidHoliday;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.service.CompanyPaidHolidayService;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.timeoff.service.TimeOffPolicyUserService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;

@Component
public class UserPermissionUtils extends BasePermissionUtils {

  private final UserService userService;

  private final CompanyService companyService;

  private final TimeOffRequestService timeOffRequestService;

  private final UserAddressService userAddressService;

  private final BenefitPlanService benefitPlanService;

  private final BenefitPlanDependentService benefitPlanDependentService;

  private final TimeOffPolicyUserService timeOffPolicyUserService;

  private final TimeOffPolicyService timeOffPolicyService;

  private final PaidHolidayService paidHolidayService;

  private final CompanyPaidHolidayService companyPaidHolidayService;

  @Autowired
  public UserPermissionUtils(final UserService userService,
      final CompanyService companyService,
      final TimeOffRequestService timeOffRequestService,
      final UserAddressService userAddressService,
      final BenefitPlanService benefitPlanService,
      final BenefitPlanDependentService benefitPlanDependentService,
      final TimeOffPolicyUserService timeOffPolicyUserService,
      final TimeOffPolicyService timeOffPolicyService,
      final PaidHolidayService paidHolidayService,
      final CompanyPaidHolidayService companyPaidHolidayService) {
    this.userService = userService;
    this.companyService = companyService;
    this.timeOffRequestService = timeOffRequestService;
    this.userAddressService = userAddressService;
    this.benefitPlanService = benefitPlanService;
    this.benefitPlanDependentService = benefitPlanDependentService;
    this.timeOffPolicyUserService = timeOffPolicyUserService;
    this.timeOffPolicyService = timeOffPolicyService;
    this.paidHolidayService = paidHolidayService;
    this.companyPaidHolidayService = companyPaidHolidayService;
  }

  boolean hasPermission(final Authentication auth, final String targetId, final Type targetType,
      final Permission.Name permission, final BaseEntity... entities) {
    if (targetId == null) {
      return false;
    }

    switch (targetType) {
      case DEPARTMENT:
        return hasPermissionOfDepartment(auth, targetId, permission);
      case BENEFIT_PLAN:
        return hasPermissionOfBenefitPlan(auth, targetId, permission);
      case TIME_OFF_REQUEST:
        return hasPermissionOfTimeOffRequest(auth, targetId, permission);
      case USER_PERSONAL_INFORMATION:
        return hasPermissionOfPersonalInformation(auth, targetId, permission);
      case USER_ADDRESS:
        return hasPermissionOfUserAddress(auth, targetId, permission);
      case USER_CONTACT_INFORMATION:
        return hasPermissionOfContactInformation(auth, targetId, permission);
      case BENEFIT_DEPENDENT:
        return hasPermissionOfBenefitDependent(auth, targetId, permission);
      case TIME_OFF_POLICY_USER:
        return hasPermissionOfTimeOffPolicyUser(auth, targetId, permission);
      case TIME_OFF_POLICY:
        return hasPermissionOfTimeOffPolicy(auth, targetId, permission);
      case PAID_HOLIDAY:
        return hasPermissionOfPaidHoliday(auth, targetId, permission);
      case COMPANY_PAID_HOLIDAY:
        return hasPermissionOfCompanyPaidHoliday(auth, targetId, permission, entities);
      case USER:
      default:
        final User targetUser = userService.findUserById(targetId);
        return hasPermissionOfUser(auth, targetUser, permission);
    }
  }

  private boolean hasPermission(final Authentication authentication,
      final Permission.Name permission) {

    final Collection<GrantedAuthority> authorities =
        (Collection<GrantedAuthority>) authentication.getAuthorities();

    return hasPermission(authorities, permission);
  }

  private boolean hasPermission(final Collection<GrantedAuthority> authorities,
      final Permission.Name permission) {
    return authorities.stream()
        .anyMatch(authority -> authority.getAuthority().equals(permission.name()));
  }

  boolean hasPermission(final Authentication auth, final List targets, final Type type,
      final Permission.Name permission) {
    if (targets.isEmpty()) {
      return hasPermission(auth, permission);
    }

    if (!(targets.get(0) instanceof BaseAuthorityDto)) {
      return false;
    }

    final List<BaseAuthorityDto> baseAuthorityDtos = (List<BaseAuthorityDto>) targets;
    for (final BaseAuthorityDto dto : baseAuthorityDtos) {
      final String id = dto.getId();
      BaseEntity[] entities = {};
      switch (type) {
        case COMPANY_PAID_HOLIDAY:
          entities = companyPaidHolidayService.findAllByCompanyId(getCompanyId()).stream()
              .map(companyPaidHoliday -> (BaseEntity) companyPaidHoliday)
              .toArray(BaseEntity[]::new);
          break;
        default:
          break;
      }
      return hasPermission(auth, id, type, permission, entities);
    }
    return false;
  }

  private void companyEqual(final Company company) {
    if (!getCompanyId().equals(company.getId())) {
      throw new ForbiddenException("The target resources is not in the company where you are.");
    }
  }

  private boolean hasPermissionOfBenefitPlan(final Authentication auth, final String id,
      final Permission.Name permission) {
    final BenefitPlan targetBenefitPlan = benefitPlanService.findBenefitPlanById(id);
    companyEqual(targetBenefitPlan.getCompany());

    return hasPermission(auth, permission);
  }

  private boolean hasPermissionOfDepartment(final Authentication auth, final String id,
      final Permission.Name permission) {
    final Department department = companyService.getDepartmentsById(id);
    companyEqual(department.getCompany());

    return hasPermission(auth, permission);
  }

  private boolean hasPermissionOfBenefitDependent(final Authentication auth, final String id,
      final Permission.Name permission) {
    final BenefitPlanDependent targetDependent = benefitPlanDependentService.findDependentById(id);
    return hasPermissionOfUser(auth, targetDependent.getEmployee(), permission);
  }

  private boolean hasPermissionOfTimeOffRequest(final Authentication auth, final String id,
      final Permission.Name permission) {
    if (permission == Name.CREATE_AND_APPROVED_TIME_OFF_REQUEST) {
      final User user = userService.findUserById(id);
      return user.getManagerUser() == null
          || getAuthUser().getId().equals(user.getManagerUser().getId())
          || getAuthUser().getRole() == User.Role.ADMIN;
    } else {
      final User user = timeOffRequestService.getById(id).getRequesterUser();
      return hasPermissionOfUser(auth, user, permission);
    }
  }

  private boolean hasPermissionOfPersonalInformation(
      final Authentication auth, final String id, final Permission.Name permission) {
    final User user = userService.findUserByUserPersonalInformationId(id);

    return hasPermissionOfUser(auth, user, permission);
  }

  private boolean hasPermissionOfUserAddress(
      final Authentication auth, final String id, final Permission.Name permission) {
    final User user = userAddressService.findUserAddressById(id).getUser();
    return hasPermissionOfUser(auth, user, permission);
  }

  private boolean hasPermissionOfContactInformation(
      final Authentication auth, final String id, final Permission.Name permission) {
    final User user = userService.findUserByUserContactInformationId(id);
    return hasPermissionOfUser(auth, user, permission);
  }

  private boolean hasPermissionOfTimeOffPolicyUser(final Authentication auth,
      final String targetId, final Name permission) {
    final TimeOffPolicyUser policyUser = timeOffPolicyUserService.findById(targetId);

    return hasPermissionOfUser(auth, policyUser.getUser(), permission);
  }

  private boolean hasPermissionOfTimeOffPolicy(
      final Authentication auth, final String policyId, final Permission.Name permission) {
    final TimeOffPolicy timeOffPolicy = timeOffPolicyService.getTimeOffPolicyById(policyId);
    final Company company = timeOffPolicy.getCompany();
    companyEqual(company);
    return hasPermission(auth, permission);
  }

  private boolean hasPermissionOfPaidHoliday(
      final Authentication auth, final String policyId, final Permission.Name permission) {
    final PaidHoliday paidHoliday = paidHolidayService.getPaidHoliday(policyId);
    final Company company = paidHoliday.getCompany();
    companyEqual(company);
    return hasPermission(auth, permission)
        && paidHoliday.getCreator().getId().equals(getAuthUser().getId());
  }

  private CompanyPaidHoliday getCompanyPaidHoliday(final String paidHolidayId,
      final BaseEntity... entities) {
    CompanyPaidHoliday companyPaidHoliday = null;
    if (entities.length != 0) {
      companyPaidHoliday = Stream.of(entities)
          .map(entity -> (CompanyPaidHoliday) entity)
          .filter(holiday -> holiday.getPaidHoliday().getId().equals(paidHolidayId))
          .findAny().orElse(null);
    }

    if (companyPaidHoliday == null) {
      companyPaidHoliday = companyPaidHolidayService
          .findCompanyPaidHolidayByPaidHolidayIdAndCompanyId(paidHolidayId, getCompanyId());
    }
    return companyPaidHoliday;
  }

  private boolean hasPermissionOfCompanyPaidHoliday(
      final Authentication auth, final String companyPaidHolidayId,
      final Permission.Name permission,
      final BaseEntity... entities) {

    final CompanyPaidHoliday companyPaidHoliday = getCompanyPaidHoliday(companyPaidHolidayId,
        entities);

    if (companyPaidHoliday == null) {
      return false;
    }

    final PaidHoliday paidHoliday = companyPaidHoliday.getPaidHoliday();
    if (paidHoliday.getCompany() == null) {
      return hasPermission(auth, permission);
    }

    return hasPermission(auth, permission) && getAuthUser().getId()
        .equals(paidHoliday.getCreator().getId());
  }

  private boolean hasPermissionOfUser(final Authentication auth, final User targetUser,
      final Permission.Name permission) {
    final PermissionType permissionType = permission.getPermissionType();
    if (permissionType == PermissionType.SELF_PERMISSION) {
      return getAuthUser().getId().equals(targetUser.getId());
    }

    companyEqual(targetUser.getCompany());

    final Collection<GrantedAuthority> authorities =
        (Collection<GrantedAuthority>) auth.getAuthorities();

    final boolean isCompanyAdmin = hasPermission(authorities, Name.MANAGE_COMPANY_USER);
    final boolean isCompanyManager = hasPermission(authorities, Name.MANAGE_TEAM_USER);

    if (permissionType == PermissionType.ADMIN_PERMISSION) {
      return hasPermission(authorities, permission);
    }

    if (permissionType == PermissionType.MANAGER_PERMISSION
        && (isCompanyManager || isCompanyAdmin)) {
      if (isCompanyAdmin) {
        return hasPermission(authorities, permission);
      }

      final String managerUserId = userService.getManagerUserIdById(targetUser.getId());
      final boolean isManager =
          !StringUtils.isEmpty(managerUserId) && managerUserId.equals(getAuthUser().getId());
      return isManager && hasPermission(authorities, permission);
    }

    return hasPermission(authorities, permission);
  }

  boolean isCurrentUserId(final String id) {
    return !StringUtils.isEmpty(id) && id.equals(getAuthUser().getId());
  }

  boolean isCurrentUserEmail(final String email) {
    return email != null && email.equals(getAuthUser().getEmail());
  }

  boolean hasAuthority(final String permission) {

    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof DefaultJwtAuthenticationToken)) {
      return false;
    }

    final List<GrantedAuthority> authorities =
        (List<GrantedAuthority>) authentication.getAuthorities();
    return authorities.stream()
        .anyMatch(grantedAuthority -> permission.equals(grantedAuthority.getAuthority()));
  }
}
