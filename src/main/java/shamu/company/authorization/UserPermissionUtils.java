package shamu.company.authorization;

import java.util.Collection;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import shamu.company.authorization.Permission.Name;
import shamu.company.authorization.Permission.PermissionType;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.service.BenefitPlanDependentService;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.company.CompanyService;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.timeoff.entity.PaidHoliday;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.timeoff.service.TimeOffPolicyService;
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

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  private final TimeOffPolicyService timeOffPolicyService;

  private final PaidHolidayService paidHolidayService;

  @Autowired
  public UserPermissionUtils(final UserService userService,
      final CompanyService companyService,
      final TimeOffRequestService timeOffRequestService,
      final UserAddressService userAddressService,
      final BenefitPlanService benefitPlanService,
      final BenefitPlanDependentService benefitPlanDependentService,
      final TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      final TimeOffPolicyService timeOffPolicyService,
      final PaidHolidayService paidHolidayService) {
    this.userService = userService;
    this.companyService = companyService;
    this.timeOffRequestService = timeOffRequestService;
    this.userAddressService = userAddressService;
    this.benefitPlanService = benefitPlanService;
    this.benefitPlanDependentService = benefitPlanDependentService;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.timeOffPolicyService = timeOffPolicyService;
    this.paidHolidayService = paidHolidayService;
  }

  boolean hasPermission(final Authentication auth, final Long targetId, final Type targetType,
      final Permission.Name permission) {
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
        return hasPermissionOfTimeOffTimeOffPolicyUser(auth, targetId, permission);
      case TIME_OFF_POLICY:
        return hasPermissionOfTimeOffPolicy(auth, targetId, permission);
      case PAID_HOLIDAY:
        return hasPermissionOfPaidHoliday(auth, targetId, permission);
      case USER:
      default:
        final User targetUser = userService.findUserById(targetId);
        return hasPermissionOfUser(auth, targetUser, permission);
    }
  }

  private boolean hasPermission(final Collection<GrantedAuthority> authorities,
      final Permission.Name permission) {

    return authorities.stream()
        .anyMatch(authority -> authority.getAuthority().equals(permission.name()));
  }

  private boolean isAdminPermission(final Authentication auth, final Permission.Name permission) {
    final Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) auth
        .getAuthorities();

    final PermissionType permissionType = permission.getPermissionType();

    if (permissionType != PermissionType.ADMIN_PERMISSION) {
      return false;
    } else {
      return authorities.stream().anyMatch(authority -> {
        final String permissionName = authority.getAuthority();
        return permission.name().equals(permissionName);
      });
    }
  }

  private void companyEqual(final Company company) {
    if (!getCompanyId().equals(company.getId())) {
      throw new ForbiddenException("The target resources is not in the company where you are.");
    }
  }

  private boolean hasPermissionOfBenefitPlan(final Authentication auth, final Long id,
      final Permission.Name permission) {
    final BenefitPlan targetBenefitPlan = benefitPlanService.findBenefitPlanById(id);
    companyEqual(targetBenefitPlan.getCompany());

    return isAdminPermission(auth, permission);
  }

  private boolean hasPermissionOfDepartment(final Authentication auth, final Long id,
      final Permission.Name permission) {
    final Department department = companyService.getDepartmentsById(id);
    companyEqual(department.getCompany());

    return hasPermission((Collection<GrantedAuthority>) auth.getAuthorities(), permission);
  }

  private boolean hasPermissionOfBenefitDependent(final Authentication auth, final Long id,
      final Permission.Name permission) {
    final BenefitPlanDependent targetDependent = benefitPlanDependentService.findDependentById(id);
    return hasPermissionOfUser(auth, targetDependent.getEmployee(), permission);
  }

  private boolean hasPermissionOfTimeOffRequest(final Authentication auth, final Long id,
      final Permission.Name permission) {
    if (permission == Name.CREATE_AND_APPROVED_TIME_OFF_REQUEST) {
      final User user = userService.findUserById(id);
      return user.getManagerUser() == null || getAuthUserRole() == User.Role.ADMIN;
    } else {
      final User user = timeOffRequestService.getById(id).getRequesterUser();
      return hasPermissionOfUser(auth, user, permission);
    }
  }

  private boolean hasPermissionOfPersonalInformation(
      final Authentication auth, final Long id, final Permission.Name permission) {
    final User user = userService.findUserByUserPersonalInformationId(id);

    return hasPermissionOfUser(auth, user, permission);
  }

  private boolean hasPermissionOfUserAddress(
      final Authentication auth, final Long id, final Permission.Name permission) {
    final User user = userAddressService.findUserAddressById(id).getUser();
    return hasPermissionOfUser(auth, user, permission);
  }

  private boolean hasPermissionOfContactInformation(
      final Authentication auth, final Long id, final Permission.Name permission) {
    final User user = userService.findUserByUserContactInformationId(id);
    return hasPermissionOfUser(auth, user, permission);
  }

  private boolean hasPermissionOfTimeOffTimeOffPolicyUser(final Authentication auth,
      final Long targetId, final Name permission) {
    final Optional<TimeOffPolicyUser> policyUser = timeOffPolicyUserRepository
        .findById(targetId);

    return policyUser.filter(
        timeOffPolicyUser -> hasPermissionOfUser(auth, timeOffPolicyUser.getUser(), permission))
        .isPresent();
  }

  private boolean hasPermissionOfTimeOffPolicy(
      final Authentication auth, final Long policyId, final Permission.Name permission) {
    final TimeOffPolicy timeOffPolicy = timeOffPolicyService.getTimeOffPolicyById(policyId);
    final Company company = timeOffPolicy.getCompany();
    companyEqual(company);
    return isAdminPermission(auth, permission);
  }

  private boolean hasPermissionOfPaidHoliday(
      final Authentication auth, final Long policyId, final Permission.Name permission) {
    final PaidHoliday paidHoliday = paidHolidayService.getPaidHoliday(policyId);
    final Company company = paidHoliday.getCompany();
    companyEqual(company);
    return isAdminPermission(auth, permission);
  }

  boolean hasPermissionOfUser(final Authentication auth, final User targetUser,
      final Permission.Name permission) {
    final PermissionType permissionType = permission.getPermissionType();
    if (permissionType == PermissionType.SELF_PERMISSION) {
      return getAuthUser().getId().equals(targetUser.getId());
    } else if (permission == Name.VIEW_TEAM_TIME_OFF_REQUEST) {
      return targetUser.getCompany().getId() == getCompanyId();
    }

    companyEqual(targetUser.getCompany());

    final Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) auth
        .getAuthorities();

    final boolean isCompanyAdmin = hasPermission(authorities, Name.MANAGE_COMPANY_USER);

    if (permissionType != PermissionType.MANAGER_PERMISSION) {
      return hasPermission(authorities, permission);
    }

    if (isCompanyAdmin) {
      return hasPermission(authorities, permission);
    }

    final Long managerUserId = userService.getManagerUserIdById(targetUser.getId());
    final boolean isManager = managerUserId != null && managerUserId.equals(getAuthUser().getId());
    return hasPermission(authorities, permission) && isManager;
  }
}
