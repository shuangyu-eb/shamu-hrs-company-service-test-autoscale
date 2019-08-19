package shamu.company.authorization;

import java.util.Collection;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import shamu.company.authorization.Permission.Name;
import shamu.company.authorization.Permission.PermissionType;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.service.BenefitPlanDependentService;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.company.entity.Company;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;

@Component
public class PermissionUtils {

  private final UserService userService;

  private final TimeOffRequestService timeOffRequestService;

  private final UserAddressService userAddressService;

  private final BenefitPlanService benefitPlanService;

  private final BenefitPlanDependentService benefitPlanDependentService;

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  @Autowired
  public PermissionUtils(final UserService userService,
      final TimeOffRequestService timeOffRequestService,
      final UserAddressService userAddressService,
      final BenefitPlanService benefitPlanService,
      final BenefitPlanDependentService benefitPlanDependentService,
      final TimeOffPolicyUserRepository timeOffPolicyUserRepository) {
    this.userService = userService;
    this.timeOffRequestService = timeOffRequestService;
    this.userAddressService = userAddressService;
    this.benefitPlanService = benefitPlanService;
    this.benefitPlanDependentService = benefitPlanDependentService;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
  }

  boolean hasPermission(final Authentication auth, final Long targetId, final Type targetType,
      final Permission.Name permission) {

    switch (targetType) {
      case BENEFIT_PLAN:
        return this.hasPermissionOfBenefitPlan(auth, targetId, permission);
      case TIME_OFF_REQUEST:
        return this.hasPermissionOfTimeOffRequest(auth, targetId, permission);
      case USER_PERSONAL_INFORMATION:
        return this.hasPermissionOfPersonalInformation(auth, targetId, permission);
      case USER_ADDRESS:
        return this.hasPermissionOfUserAddress(auth, targetId, permission);
      case USER_CONTACT_INFORMATION:
        return this.hasPermissionOfContactInformation(auth, targetId, permission);
      case BENEFIT_DEPENDENT:
        return this.hasPermissionOfBenefitDependent(auth, targetId, permission);
      case TIME_OFF_POLICY_USER:
        return this.hasPermissionOfTimeOffTimeOffPolicyUser(auth, targetId, permission);
      case USER:
      default:
        final User targetUser = userService.findUserById(targetId);
        return this.hasPermissionOfUser(auth, targetUser, permission);
    }
  }

  private boolean hasPermission(final Collection<AuthorityPojo> authorities,
      final Permission.Name permission) {

    return authorities.stream()
        .anyMatch(authority -> authority.getName() == permission);
  }

  private void companyEqual(final Authentication auth, final Company company) {
    if (!this.getCompany(auth).getId().equals(company.getId())) {
      throw new ForbiddenException("The target resources is not in the company where you are.");
    }
  }

  private boolean hasPermissionOfBenefitPlan(final Authentication auth, final Long id,
      final Permission.Name permission) {
    final PermissionType permissionType = permission.getPermissionType();

    final BenefitPlan targetBenefitPlan = benefitPlanService.findBenefitPlanById(id);
    this.companyEqual(auth, targetBenefitPlan.getCompany());

    final boolean result;
    final Collection<AuthorityPojo> authorities = (Collection<AuthorityPojo>) auth.getAuthorities();
    if (permissionType != PermissionType.ADMIN_PERMISSION) {
      result = false;
    } else {
      result = authorities.stream().anyMatch(authority -> {
        final Permission.Name permissionName = authority.getName();
        return permission == permissionName;
      });
    }

    return result;
  }

  private boolean hasPermissionOfBenefitDependent(final Authentication auth, final Long id,
      final Permission.Name permission) {
    final BenefitPlanDependent targetDependent = benefitPlanDependentService.findDependentById(id);
    return this.hasPermissionOfUser(auth,targetDependent.getEmployee(),permission);
  }

  private boolean hasPermissionOfTimeOffRequest(final Authentication auth, final Long id,
      final Permission.Name permission) {
    final User user = timeOffRequestService.getById(id).getRequesterUser();
    return this.hasPermissionOfUser(auth, user, permission);
  }

  private boolean hasPermissionOfPersonalInformation(
      final Authentication auth, final Long id, final Permission.Name permission) {
    final User user = userService.findUserByUserPersonalInformationId(id);

    return this.hasPermissionOfUser(auth, user, permission);
  }

  private boolean hasPermissionOfUserAddress(
      final Authentication auth, final Long id, final Permission.Name permission) {
    final User user = userAddressService.findUserAddressById(id).getUser();
    return this.hasPermissionOfUser(auth, user, permission);
  }

  private boolean hasPermissionOfContactInformation(
      final Authentication auth, final Long id, final Permission.Name permission) {
    final User user = userService.findUserByUserContactInformationId(id);
    return this.hasPermissionOfUser(auth, user, permission);
  }

  private boolean hasPermissionOfTimeOffTimeOffPolicyUser(final Authentication auth,
      final Long targetId, final Name permission) {
    final Optional<TimeOffPolicyUser> policyUser = timeOffPolicyUserRepository
        .findById(targetId);

    return policyUser.filter(timeOffPolicyUser -> this
        .hasPermissionOfUser(auth, timeOffPolicyUser.getUser(), permission)).isPresent();
  }

  private boolean hasPermissionOfUser(final Authentication auth, final User targetUser,
      final Permission.Name permission) {
    final PermissionType permissionType = permission.getPermissionType();
    if (permissionType == PermissionType.SELF_PERMISSION) {
      return this.getUser(auth).getId().equals(targetUser.getId());
    }

    this.companyEqual(auth, targetUser.getCompany());

    final boolean result;
    final Collection<AuthorityPojo> authorities = (Collection<AuthorityPojo>) auth.getAuthorities();
    if (permissionType != PermissionType.MANAGER_PERMISSION) {
      result = hasPermission(authorities, permission);
    } else {
      result = authorities.stream().anyMatch(p -> {
            final Permission.Name permissionName = p.getName();
            if (permission == permissionName) {
              if (p.getIds() != null) {
                return p.getIds().contains(targetUser.getId());
              }
              return true;
            }
            return false;
          }
      );
    }

    return result;
  }

  private User getUser(final Authentication auth) {
    return (User) auth.getPrincipal();
  }

  private Company getCompany(final Authentication auth) {
    return this.getUser(auth).getCompany();
  }
}
