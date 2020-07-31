package shamu.company.job.service;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.common.exception.errormapping.AlreadyExistsException;
import shamu.company.common.service.DepartmentService;
import shamu.company.common.service.OfficeAddressService;
import shamu.company.common.service.OfficeService;
import shamu.company.company.dto.OfficeAddressDto;
import shamu.company.company.dto.OfficeCreateDto;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.company.entity.mapper.OfficeAddressMapper;
import shamu.company.company.entity.mapper.OfficeMapper;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.SelectFieldSizeDto;
import shamu.company.job.dto.JobSelectOptionUpdateDto;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.dto.JobUserHireDateCheckDto;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.exception.errormapping.DeletionFailedCausedByCascadeException;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.server.dto.AuthUser;
import shamu.company.timeoff.dto.MyTimeOffDto;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.dto.TimeOffRequestUpdateDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.repository.TimeOffPolicyAccrualScheduleRepository;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.dto.UserOfficeAndHomeAddressDto;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.service.CompensationOvertimeStatusService;
import shamu.company.user.service.UserCompensationService;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobUserService {

  private final JobUserRepository jobUserRepository;

  private final UserService userService;

  private final JobUserMapper jobUserMapper;

  private final UserCompensationMapper userCompensationMapper;

  private final UserRoleService userRoleService;

  private final TimeOffRequestService timeOffRequestService;

  private final DepartmentService departmentService;

  private final OfficeService officeService;

  private final OfficeAddressService officeAddressService;

  private final OfficeAddressMapper officeAddressMapper;

  private final OfficeMapper officeMapper;

  private final JobService jobService;

  private final UserMapper userMapper;

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  private final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository;

  private final UserAddressRepository userAddressRepository;

  private final UserAddressMapper userAddressMapper;

  private final TimeOffPolicyService timeOffPolicyService;

  private final CompensationOvertimeStatusService compensationOvertimeStatusService;

  private final TimePeriodService timePeriodService;

  private final UserCompensationService userCompensationService;

  public JobUserService(
      final JobUserRepository jobUserRepository,
      final UserService userService,
      final JobUserMapper jobUserMapper,
      final UserCompensationMapper userCompensationMapper,
      final UserRoleService userRoleService,
      final TimeOffRequestService timeOffRequestService,
      final DepartmentService departmentService,
      final OfficeService officeService,
      final OfficeAddressService officeAddressService,
      final OfficeAddressMapper officeAddressMapper,
      final OfficeMapper officeMapper,
      final JobService jobService,
      final UserMapper userMapper,
      final TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository,
      final UserAddressRepository userAddressRepository,
      final UserAddressMapper userAddressMapper,
      final TimeOffPolicyService timeOffPolicyService,
      final CompensationOvertimeStatusService compensationOvertimeStatusService,
      final TimePeriodService timePeriodService,
      final UserCompensationService userCompensationService) {
    this.jobUserRepository = jobUserRepository;
    this.userService = userService;
    this.userCompensationMapper = userCompensationMapper;
    this.jobUserMapper = jobUserMapper;
    this.userRoleService = userRoleService;
    this.timeOffRequestService = timeOffRequestService;
    this.departmentService = departmentService;
    this.officeService = officeService;
    this.officeAddressService = officeAddressService;
    this.officeAddressMapper = officeAddressMapper;
    this.officeMapper = officeMapper;
    this.jobService = jobService;
    this.userMapper = userMapper;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.timeOffPolicyAccrualScheduleRepository = timeOffPolicyAccrualScheduleRepository;
    this.timeOffPolicyService = timeOffPolicyService;
    this.userAddressRepository = userAddressRepository;
    this.userAddressMapper = userAddressMapper;
    this.compensationOvertimeStatusService = compensationOvertimeStatusService;
    this.timePeriodService = timePeriodService;
    this.userCompensationService = userCompensationService;
  }

  public JobUser save(final JobUser jobUser) {
    return jobUserRepository.save(jobUser);
  }

  public JobUser getJobUserByUserId(final String userId) {
    return jobUserRepository.findByUserId(userId);
  }

  private boolean userHasNoneOrDifferentManager(final User user, final String managerId) {
    if (StringUtils.isEmpty(managerId)) {
      return false;
    }
    return user.getManagerUser() == null || !user.getManagerUser().getId().equals(managerId);
  }

  private void adjustManagerLocationInOrganizationRelationship(
      final User user, final String managerId) {
    final User manager = userService.findById(managerId);
    if (Role.EMPLOYEE == manager.getRole()) {
      manager.setUserRole(userRoleService.getManager());
    }
    if (isSubordinate(managerId, user.getId())) {
      manager.setManagerUser(user.getManagerUser());
    }
    userService.save(manager);
    user.setManagerUser(manager);
  }

  private void adjustUserLocationInOrganizationRelationship(
      final User user, final String companyId, final String managerId) {
    final List<User> subordinates =
        userService.findSubordinatesByManagerUserId(companyId, user.getId());
    if (user.getRole() != Role.ADMIN) {
      subordinates.removeIf(employee -> employee.getId().equals(managerId));
      final UserRole targetUserRole =
          subordinates.isEmpty() ? userRoleService.getEmployee() : userRoleService.getManager();
      user.setUserRole(targetUserRole);
    }
  }

  private void addOrUpdateUserManager(
      final User user, final String companyId, final String managerId) {
    if (null == managerId) {
      user.setManagerUser(null);
      userService.save(user);
    } else if (userHasNoneOrDifferentManager(user, managerId)) {
      adjustManagerLocationInOrganizationRelationship(user, managerId);
      adjustUserLocationInOrganizationRelationship(user, companyId, managerId);
      handlePendingRequests(managerId);
      userService.save(user);
    }
  }

  private boolean jobUserCompensationUpdated(final JobUpdateDto jobUpdateDto) {
    return jobUpdateDto.getCompensationWage() != null
        && jobUpdateDto.getCompensationFrequencyId() != null;
  }

  private void addOrUpdateJobUserCompensation(
      final String userId, final JobUpdateDto jobUpdateDto, final JobUser jobUser) {
    if (jobUserCompensationUpdated(jobUpdateDto) || jobUpdateDto.getPayTypeName() != null) {
      UserCompensation userCompensation = jobUser.getUserCompensation();
      final Optional<TimePeriod> userCurrentPeriod = timePeriodService.findUserLatestPeriod(userId);
      if (userCompensation == null) {
        userCompensation = new UserCompensation();
      } else if (userCurrentPeriod.isPresent()) {
        final TimePeriod currentTimePeriod = userCurrentPeriod.get();
        if (userCompensation.getStartDate() != currentTimePeriod.getEndDate()) {
          userCompensation.setEndDate(currentTimePeriod.getEndDate());
          userCompensationService.save(userCompensation);
          userCompensation = new UserCompensation();
          userCompensation.setStartDate(currentTimePeriod.getEndDate());
        }
      }

      if (jobUserCompensationUpdated(jobUpdateDto)) {
        userCompensationMapper.updateFromJobUpdateDto(userCompensation, jobUpdateDto);
      }
      if (jobUpdateDto.getPayTypeName() != null) {
        final CompensationOvertimeStatus compensationOvertimeStatus =
            compensationOvertimeStatusService.findByName(jobUpdateDto.getPayTypeName());
        userCompensation.setOvertimeStatus(compensationOvertimeStatus);
      }
      userCompensation.setUserId(userId);
      jobUser.setUserCompensation(userCompensation);
    }
  }

  private void addOrUpdateJobUser(final User user, final JobUpdateDto jobUpdateDto) {
    JobUser jobUser = findJobUserByUser(user);
    if (null == jobUser) {
      jobUser = new JobUser();
      jobUser.setUser(user);
    }
    jobUserMapper.updateFromJobUpdateDto(jobUser, jobUpdateDto);
    addOrUpdateJobUserCompensation(user.getId(), jobUpdateDto, jobUser);
    jobUserRepository.save(jobUser);
  }

  public void updateJobInfo(
      final String id, final JobUpdateDto jobUpdateDto, final String companyId) {
    final User user = userService.findById(id);
    addOrUpdateUserManager(user, companyId, jobUpdateDto.getManagerId());
    addOrUpdateJobUser(user, jobUpdateDto);
  }

  // The 'userId' represents employee A, 'managerId' represents employee B.
  // This function is to figure out if A is B's subordinate.
  private boolean isSubordinate(final String userId, final String managerId) {
    User user = userService.findById(userId);
    while (user.getManagerUser() != null && !user.getManagerUser().getId().equals(managerId)) {
      final String userManagerId = user.getManagerUser().getId();
      user = userService.findById(userManagerId);
    }
    return user.getManagerUser() != null && user.getManagerUser().getId().equals(managerId);
  }

  private void handlePendingRequests(final String userId) {
    final Timestamp startDayTimestamp = DateUtil.getFirstDayOfCurrentYear();
    final String[] timeOffRequestStatuses =
        new String[] {TimeOffApprovalStatus.NO_ACTION.name(), TimeOffApprovalStatus.VIEWED.name()};
    final PageRequest request = PageRequest.of(0, 1000);
    final MyTimeOffDto pendingRequests =
        timeOffRequestService.getMyTimeOffRequestsByRequesterUserIdFilteredByStartDay(
            userId, startDayTimestamp, timeOffRequestStatuses, request);
    if (null != pendingRequests && pendingRequests.getTimeOffRequests().hasContent()) {
      final List<TimeOffRequestDto> timeOffRequests =
          pendingRequests.getTimeOffRequests().getContent();
      final AuthUser authUser = new AuthUser();
      authUser.setId(userId);
      timeOffRequests.forEach(
          t -> {
            final TimeOffRequestUpdateDto timeOffRequestUpdateDto = new TimeOffRequestUpdateDto();
            timeOffRequestUpdateDto.setStatus(TimeOffApprovalStatus.APPROVED);
            timeOffRequestService.updateTimeOffRequestStatus(
                t.getId(), timeOffRequestUpdateDto, authUser);
          });
    }
  }

  public void updateJobSelectOption(final JobSelectOptionUpdateDto jobSelectOptionUpdateDto) {
    final String id = jobSelectOptionUpdateDto.getId();
    final String name = jobSelectOptionUpdateDto.getNewName();

    switch (jobSelectOptionUpdateDto.getUpdateField()) {
      case DEPARTMENT:
        updateDepartmentName(id, name);
        break;
      case JOB_TITLE:
        updateJobTitleName(id, name);
        break;
      case OFFICE_LOCATION:
        updateOfficeContent(id, jobSelectOptionUpdateDto.getOfficeCreateDto());
        break;
      default:
        break;
    }
  }

  private void updateDepartmentName(final String id, final String name) {
    final Department department = departmentService.findById(id);
    final List<Department> oldDepartments =
        departmentService.findByNameAndCompanyId(name, department.getCompany().getId());
    if (!oldDepartments.isEmpty()) {
      throw new AlreadyExistsException("Department already exists.", "department");
    }
    department.setName(name);
    departmentService.save(department);
  }

  private void updateJobTitleName(final String id, final String name) {
    final Job job = jobService.findById(id);
    final List<Job> oldJobs = jobService.findByTitleAndCompanyId(name, job.getCompany().getId());
    if (!oldJobs.isEmpty()) {
      throw new AlreadyExistsException("Job title already exists.", "job title");
    }
    job.setTitle(name);
    jobService.save(job);
  }

  private void updateOfficeContent(final String id, final OfficeCreateDto officeCreateDto) {
    final Office office = officeService.findById(id);
    final List<Office> oldOffices =
        officeService.findByNameAndCompanyId(
            officeCreateDto.getOfficeName(), office.getCompany().getId());
    if (!oldOffices.isEmpty()) {
      throw new AlreadyExistsException("Office already exists.", "office");
    }
    office.setName(officeCreateDto.getOfficeName());

    final OfficeAddress officeAddress =
        officeAddressMapper.updateFromOfficeCreateDto(office.getOfficeAddress(), officeCreateDto);

    final Office newOffice = officeMapper.convertToOffice(office, officeCreateDto, officeAddress);
    officeService.save(newOffice);
  }

  public void deleteJobSelectOption(final JobSelectOptionUpdateDto jobSelectOptionUpdateDto) {
    final String id = jobSelectOptionUpdateDto.getId();

    switch (jobSelectOptionUpdateDto.getUpdateField()) {
      case DEPARTMENT:
        deleteDepartment(id);
        break;
      case JOB_TITLE:
        deleteJobTitle(id);
        break;
      case OFFICE_LOCATION:
        deleteOffice(id);
        break;
      default:
        break;
    }
  }

  private void deleteDepartment(final String id) {
    final Integer count = departmentService.findCountByDepartment(id);
    if (count > 0) {
      throw new DeletionFailedCausedByCascadeException(
          "Couldn't remove department since there are one or more employee assigned to it.",
          "department");
    }
    departmentService.delete(id);
  }

  private void deleteJobTitle(final String id) {
    final Integer count = getCountByJobId(id);
    if (count > 0) {
      throw new DeletionFailedCausedByCascadeException(
          "Couldn't remove job since there are one or more employee assigned to it.", "job");
    }
    jobService.delete(id);
  }

  private void deleteOffice(final String id) {
    final Integer count = officeService.findCountByOffice(id);
    if (count > 0) {
      throw new DeletionFailedCausedByCascadeException(
          "Couldn't remove office since there are one or more employee assigned to it.", "office");
    }
    final Office office = officeService.findById(id);
    officeService.delete(id);
    officeAddressService.delete(office.getOfficeAddress());
  }

  public Integer getCountByJobId(final String jobId) {
    return jobUserRepository.getCountByJobId(jobId);
  }

  public List<SelectFieldSizeDto> findJobsByCompanyId(final String companyId) {
    final List<Job> jobs = jobService.findAllByCompanyId(companyId);
    return jobs.stream()
        .map(
            job -> {
              final SelectFieldSizeDto selectFieldSizeDto = new SelectFieldSizeDto();
              final Integer size = getCountByJobId(job.getId());
              selectFieldSizeDto.setId(job.getId());
              selectFieldSizeDto.setName(job.getTitle());
              selectFieldSizeDto.setSize(size);
              return selectFieldSizeDto;
            })
        .collect(Collectors.toList());
  }

  public JobUser findJobUserByUser(final User user) {
    return jobUserRepository.findJobUserByUser(user);
  }

  public BasicJobInformationDto findJobMessage(final String targetUserId, final String authUserId) {
    final JobUser target = getJobUserByUserId(targetUserId);

    if (target == null) {
      final User targetUser = userService.findById(targetUserId);
      return userMapper.convertToBasicJobInformationDto(targetUser);
    }

    // The user's full job message can only be accessed by admin, the manager and himself.
    final User currentUser = userService.findById(authUserId);
    final Role userRole = currentUser.getRole();
    if (authUserId.equals(targetUserId) || userRole == Role.ADMIN || userRole == Role.SUPER_ADMIN) {
      return jobUserMapper.convertToJobInformationDto(target);
    }

    if (userRole == Role.MANAGER
        && target.getUser().getManagerUser() != null
        && authUserId.equals(target.getUser().getManagerUser().getId())) {
      return jobUserMapper.convertToJobInformationDto(target);
    }

    return jobUserMapper.convertToBasicJobInformationDto(target);
  }

  public JobUserHireDateCheckDto checkUserHireDateDeletable(final String userId) {

    final User user = userService.findById(userId);

    final JobUser jobUser = findJobUserByUser(user);

    if (jobUser == null || jobUser.getStartDate() == null) {
      return new JobUserHireDateCheckDto(true);
    }

    final List<TimeOffPolicyUser> timeOffPolicyUsers =
        timeOffPolicyUserRepository.findTimeOffPolicyUsersByUser(user);

    final Boolean hireDateDeletable =
        timeOffPolicyUsers.stream()
            .noneMatch(
                timeOffPolicyUser -> {
                  final TimeOffPolicy timeOffPolicy = timeOffPolicyUser.getTimeOffPolicy();
                  if (BooleanUtils.isFalse(timeOffPolicy.getIsLimited())) {
                    return false;
                  }
                  final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
                      timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(
                          timeOffPolicyUser.getTimeOffPolicy());
                  return timeOffPolicyService.checkIsPolicyCalculationRelatedToHireDate(
                      timeOffPolicyAccrualSchedule);
                });

    return new JobUserHireDateCheckDto(hireDateDeletable);
  }

  public List<UserOfficeAndHomeAddressDto> findHomeAndOfficeAddressByUsers(
      final List<String> userIds) {
    final List<UserOfficeAndHomeAddressDto> userOfficeAndHomeAddressDtos = new ArrayList<>();

    userIds.stream()
        .forEach(
            userId -> {
              final UserAddressDto userAddressDto =
                  userAddressMapper.convertToUserAddressDto(
                      userAddressRepository.findUserAddressByUserId(userId));
              final OfficeAddressDto officeAddressDto =
                  jobUserMapper.convertToOfficeAddressDto(jobUserRepository.findByUserId(userId));

              userOfficeAndHomeAddressDtos.add(
                  UserOfficeAndHomeAddressDto.builder()
                      .userId(userId)
                      .userHomeAddress(userAddressDto)
                      .userOfficeAddress(officeAddressDto)
                      .build());
            });

    return userOfficeAndHomeAddressDtos;
  }

  public Boolean checkJobInfoComplete(final String userId) {
    final JobUser jobUser = jobUserRepository.findByUserId(userId);

    return jobUser != null && verifyJobInfoComplete(jobUser);
  }

  private boolean verifyJobInfoComplete(final JobUser employeeWithJobInfo) {
    return employeeWithJobInfo.getEmployeeType() != null
        && employeeWithJobInfo.getStartDate() != null
        && employeeWithJobInfo.getOffice() != null
        && employeeWithJobInfo.getUserCompensation() != null;
  }
}
