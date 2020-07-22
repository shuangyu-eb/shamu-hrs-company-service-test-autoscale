package shamu.company.authorization;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import shamu.company.authorization.Permission.Name;
import shamu.company.authorization.Permission.PermissionType;
import shamu.company.benefit.dto.BenefitPlanCoverageDto;
import shamu.company.benefit.entity.BenefitCoverages;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.repository.BenefitCoveragesRepository;
import shamu.company.benefit.service.BenefitPlanDependentService;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.exception.errormapping.ForbiddenException;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.NewEmployeeJobInformationDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.dto.UserIdDto;
import shamu.company.timeoff.entity.CompanyPaidHoliday;
import shamu.company.timeoff.entity.PaidHoliday;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.service.CompanyPaidHolidayService;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.timeoff.service.TimeOffPolicyUserService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;

@Component
public class UserPermissionUtils extends BasePermissionUtils {

  private static final String BENEFIT_NEW_COVERAGE = "add";

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

  private final UserEmergencyContactService userEmergencyContactService;

  @Autowired
  public UserPermissionUtils(
      final UserService userService,
      final CompanyService companyService,
      final TimeOffRequestService timeOffRequestService,
      final UserAddressService userAddressService,
      final BenefitPlanService benefitPlanService,
      final BenefitPlanDependentService benefitPlanDependentService,
      final TimeOffPolicyUserService timeOffPolicyUserService,
      final TimeOffPolicyService timeOffPolicyService,
      final PaidHolidayService paidHolidayService,
      final CompanyPaidHolidayService companyPaidHolidayService,
      final UserEmergencyContactService userEmergencyContactService,
      final BenefitCoveragesRepository benefitCoveragesRepository) {
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
    this.userEmergencyContactService = userEmergencyContactService;
  }

  boolean hasPermission(
      final Authentication auth,
      final String targetId,
      final Type targetType,
      final Permission.Name permission,
      final BaseEntity... entities) {
    if (targetId == null) {
      return false;
    }

    // TODO: Company field have been deleted in some entities, permission code need to be
    // refactored.
    switch (targetType) {
      case DEPARTMENT:
        return hasPermissionOfDepartment(auth, targetId, permission);
      case JOB_TITLE:
        return hasPermissionOfJobTitle(auth, targetId, permission);
      case OFFICE_LOCATION:
        return hasPermissionOfOfficeLocation(auth, targetId, permission);
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
      case USER_EMERGENCY_CONTACT:
        return hasPermissionOfUserEmergencyContact(auth, targetId, permission);
      case USER:
      case TIME_OFF_USER:
      default:
        final User targetUser = userService.findById(targetId);
        return hasPermissionOfUser(auth, targetUser, permission);
    }
  }

  @SuppressWarnings("unchecked")
  private boolean hasPermission(
      final Authentication authentication, final Permission.Name permission) {

    final Collection<GrantedAuthority> authorities =
        (Collection<GrantedAuthority>) authentication.getAuthorities();

    return hasPermission(authorities, permission);
  }

  private boolean hasPermission(
      final Collection<GrantedAuthority> authorities, final Permission.Name permission) {
    return authorities.stream()
        .anyMatch(authority -> authority.getAuthority().equals(permission.name()));
  }

  @SuppressWarnings("unchecked")
  boolean hasPermission(
      final Authentication auth,
      final List targets,
      final Type type,
      final Permission.Name permission) {
    boolean hasPermission;
    if (targets.isEmpty()) {
      return hasPermission(auth, permission);
    }
    switch (type) {
      case COMPANY_PAID_HOLIDAY:
        final List<PaidHolidayDto> paidHolidayDtos = (List<PaidHolidayDto>) targets;
        for (final PaidHolidayDto paidHolidayDto : paidHolidayDtos) {
          final String id = paidHolidayDto.getId();
          final BaseEntity[] entities =
              companyPaidHolidayService.findAllByCompanyId(getCompanyId()).stream()
                  .toArray(BaseEntity[]::new);
          hasPermission = hasPermission(auth, id, type, permission, entities);
          if (!hasPermission) {
            return false;
          }
        }
        return true;
      case BENEFIT_COVERAGE_CREATION:
        final List<BenefitPlanCoverageDto> benefitPlanCoverageDtos =
            (List<BenefitPlanCoverageDto>) targets;
        final List<BenefitPlanCoverageDto> coverageList =
            benefitPlanCoverageDtos.stream()
                .filter(coverage -> !coverage.getId().startsWith(BENEFIT_NEW_COVERAGE))
                .collect(Collectors.toList());
        final List<String> existIds =
            benefitPlanService.findAllByBenefitPlanIdIsNullOrderByRefIdAsc().stream()
                .map(BenefitCoverages::getId)
                .collect(Collectors.toList());
        for (final BenefitPlanCoverageDto benefitPlanCoverageDto : coverageList) {
          if (!existIds.contains(benefitPlanCoverageDto.getId())) {
            return false;
          }
        }
        return true;
      case TIME_OFF_USER:
      case PAID_HOLIDAY_USER:
        return authByUserIds(auth, (List<UserIdDto>) targets, type, permission);
      default:
        return false;
    }
  }

  private boolean authByUserIds(
      final Authentication authentication,
      final List<UserIdDto> userIdDtos,
      final Type type,
      final Permission.Name permission) {
    for (final UserIdDto userIdDto : userIdDtos) {
      final String userId = userIdDto.getUserId();
      if (!hasPermission(authentication, userId, type, permission)) {
        return false;
      }
    }

    return true;
  }

  boolean hasPermissionOfObjectTarget(
      final Authentication auth,
      final Object target,
      final Type type,
      final Permission.Name permission) {
    if (Type.USER_JOB.equals(type)) {
      return hasPermissionOfUserJob(target);
    }

    if (Type.USER_CREATION.equals(type)) {
      final EmployeeDto employeeDto = (EmployeeDto) target;
      final NewEmployeeJobInformationDto jobInformationDto = employeeDto.getJobInformation();
      final String managerId = jobInformationDto.getReportsTo();
      if (!StringUtils.isEmpty(managerId)) {
        final User manager = userService.findById(managerId);

        if (!hasPermissionOfUser(auth, manager, permission)) {
          return false;
        }
      }

      final String officeId = jobInformationDto.getOfficeId();
      final boolean hasPermissionOfOfficeLocation =
          !StringUtils.isEmpty(officeId)
              && !hasPermissionOfOfficeLocation(auth, officeId, permission);
      return !hasPermissionOfOfficeLocation;
    }

    return hasPermission(auth, permission);
  }

  private void companyEqual(final Company company) {
    if (!getCompanyId().equals(company.getId())) {
      throw new ForbiddenException("The target resources is not in the company where you are.");
    }
  }

  private boolean hasPermissionOfUserJob(final Object target) {
    final JobUpdateDto jobUpdateDto = (JobUpdateDto) target;
    final String managerId = jobUpdateDto.getManagerId();

    if (!StringUtils.isEmpty(managerId)) {
      final User manager = userService.findById(managerId);
      companyEqual(manager.getCompany());
    }
    return true;
  }

  private boolean hasPermissionOfBenefitPlan(
      final Authentication auth, final String id, final Permission.Name permission) {
    benefitPlanService.findBenefitPlanById(id);
    return hasPermission(auth, permission);
  }

  private boolean hasPermissionOfDepartment(
      final Authentication auth, final String id, final Permission.Name permission) {
    companyService.findDepartmentsById(id);
    return hasPermission(auth, permission);
  }

  private boolean hasPermissionOfJobTitle(
      final Authentication auth, final String id, final Permission.Name permission) {
    companyService.findJobsById(id);
    return hasPermission(auth, permission);
  }

  private boolean hasPermissionOfCompany(
      final Authentication auth, final Company company, final Permission.Name permission) {
    companyEqual(company);
    return hasPermission(auth, permission);
  }

  private boolean hasPermissionOfOfficeLocation(
      final Authentication auth, final String id, final Permission.Name permission) {
    companyService.findOfficeById(id);
    return hasPermission(auth, permission);
  }

  private boolean hasPermissionOfBenefitDependent(
      final Authentication auth, final String id, final Permission.Name permission) {
    final BenefitPlanDependent targetDependent = benefitPlanDependentService.findDependentById(id);
    return hasPermissionOfUser(auth, targetDependent.getEmployee(), permission);
  }

  private boolean hasPermissionOfTimeOffRequest(
      final Authentication auth, final String id, final Permission.Name permission) {
    final String authUserId = getAuthUser().getId();

    if (permission == Name.CREATE_AND_APPROVED_TIME_OFF_REQUEST) {
      final User user = userService.findById(id);
      final User manager = user.getManagerUser();

      if (authUserId.equals(id)) {
        return manager == null && hasPermissionOfUser(auth, user, permission);
      }

      if (manager != null && authUserId.equals(manager.getId())) {
        return hasPermissionOfUser(auth, user, permission);
      }

      return getAuthUser().getRole() == User.Role.ADMIN
          && hasPermissionOfUser(auth, user, permission);
    }

    final User user = timeOffRequestService.getById(id).getRequesterUser();
    if (permission == Name.MANAGE_TIME_OFF_REQUEST) {
      return (!authUserId.equals(user.getId()) && hasPermissionOfUser(auth, user, permission))
          || (authUserId.equals(user.getId())
              && (Role.ADMIN.equals(user.getRole()) || Role.SUPER_ADMIN.equals(user.getRole()))
              && user.getManagerUser() == null);
    }

    return hasPermissionOfUser(auth, user, permission);
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

  private boolean hasPermissionOfTimeOffPolicyUser(
      final Authentication auth, final String targetId, final Name permission) {
    final TimeOffPolicyUser policyUser = timeOffPolicyUserService.findById(targetId);

    return hasPermissionOfUser(auth, policyUser.getUser(), permission);
  }

  private boolean hasPermissionOfTimeOffPolicy(
      final Authentication auth, final String policyId, final Permission.Name permission) {
    timeOffPolicyService.getTimeOffPolicyById(policyId);
    return hasPermission(auth, permission);
  }

  private boolean hasPermissionOfPaidHoliday(
      final Authentication auth, final String policyId, final Permission.Name permission) {
    final PaidHoliday paidHoliday = paidHolidayService.getPaidHoliday(policyId);
    final User creator = paidHoliday.getCreator();
    if (creator == null) {
      return false;
    }

    return hasPermission(auth, permission)
        && paidHoliday.getCreator().getId().equals(getAuthUser().getId());
  }

  private CompanyPaidHoliday getCompanyPaidHoliday(
      final String paidHolidayId, final BaseEntity... entities) {
    CompanyPaidHoliday companyPaidHoliday = null;
    if (entities.length != 0) {
      companyPaidHoliday =
          Stream.of(entities)
              .map(entity -> (CompanyPaidHoliday) entity)
              .filter(holiday -> holiday.getPaidHoliday().getId().equals(paidHolidayId))
              .findAny()
              .orElse(null);
    }

    if (companyPaidHoliday == null) {
      companyPaidHoliday =
          companyPaidHolidayService.findCompanyPaidHolidayByPaidHolidayIdAndCompanyId(
              paidHolidayId, getCompanyId());
    }
    return companyPaidHoliday;
  }

  private boolean hasPermissionOfCompanyPaidHoliday(
      final Authentication auth,
      final String companyPaidHolidayId,
      final Permission.Name permission,
      final BaseEntity... entities) {

    final CompanyPaidHoliday companyPaidHoliday =
        getCompanyPaidHoliday(companyPaidHolidayId, entities);

    if (companyPaidHoliday == null) {
      return false;
    }

    final PaidHoliday paidHoliday = companyPaidHoliday.getPaidHoliday();

    return hasPermission(auth, permission)
        && getAuthUser().getId().equals(paidHoliday.getCreator().getId());
  }

  private boolean hasPermissionOfUserEmergencyContact(
      final Authentication auth, final String id, final Permission.Name permission) {
    final UserEmergencyContact userEmergencyContact = userEmergencyContactService.findById(id);
    final User targetUser = userEmergencyContact.getUser();
    companyEqual(targetUser.getCompany());

    if (permission.getPermissionType() == PermissionType.SELF_PERMISSION) {
      return getUserId().equals(targetUser.getId());
    }

    return hasPermission(auth, permission);
  }

  @SuppressWarnings("unchecked")
  private boolean hasPermissionOfUser(
      final Authentication auth, final User targetUser, final Permission.Name permission) {
    final PermissionType permissionType = permission.getPermissionType();
    final Collection<GrantedAuthority> authorities =
        (Collection<GrantedAuthority>) auth.getAuthorities();

    if (permissionType == PermissionType.SELF_PERMISSION) {
      return getAuthUser().getId().equals(targetUser.getId())
          && hasPermission(authorities, permission);
    }

    companyEqual(targetUser.getCompany());

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

      final Optional<User> manager = Optional.ofNullable(targetUser.getManagerUser());
      final String managerUserId = manager.orElse(new User()).getId();
      final boolean isManager =
          !StringUtils.isEmpty(managerUserId)
              && managerUserId.equalsIgnoreCase(getAuthUser().getId());
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

  @SuppressWarnings("unchecked")
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
