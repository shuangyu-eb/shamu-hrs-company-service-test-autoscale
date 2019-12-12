package shamu.company.job.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.common.repository.EmploymentTypeRepository;
import shamu.company.common.repository.OfficeAddressRepository;
import shamu.company.common.repository.OfficeRepository;
import shamu.company.company.dto.OfficeCreateDto;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.company.entity.mapper.OfficeAddressMapper;
import shamu.company.company.entity.mapper.OfficeMapper;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.dto.JobSelectOptionUpdateDto;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobRepository;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.server.dto.AuthUser;
import shamu.company.timeoff.dto.MyTimeOffDto;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.dto.TimeOffRequestUpdateDto;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;

@Service
public class JobUserService {

  private final JobUserRepository jobUserRepository;

  private final UserService userService;

  private final JobUserMapper jobUserMapper;

  private final UserCompensationMapper userCompensationMapper;

  private final EmployeeService employeeService;

  private final UserRoleService userRoleService;

  private final TimeOffRequestService timeOffRequestService;

  private final DepartmentRepository departmentRepository;

  private final JobRepository jobRepository;

  private final EmploymentTypeRepository employmentTypeRepository;

  private final OfficeRepository officeRepository;

  private final OfficeAddressRepository officeAddressRepository;

  private final OfficeAddressMapper officeAddressMapper;

  private final OfficeMapper officeMapper;



  public JobUserService(final JobUserRepository jobUserRepository,
      final UserService userService,
      final JobUserMapper jobUserMapper,
      final UserCompensationMapper userCompensationMapper,
      final EmployeeService employeeService,
      final UserRoleService userRoleService,
      final TimeOffRequestService timeOffRequestService,
      final DepartmentRepository departmentRepository,
      final JobRepository jobRepository,
      final EmploymentTypeRepository employmentTypeRepository,
      final OfficeRepository officeRepository,
      final OfficeAddressRepository officeAddressRepository,
      final OfficeAddressMapper officeAddressMapper,
      final OfficeMapper officeMapper) {
    this.jobUserRepository = jobUserRepository;
    this.userService = userService;
    this.userCompensationMapper = userCompensationMapper;
    this.jobUserMapper = jobUserMapper;
    this.employeeService = employeeService;
    this.userRoleService = userRoleService;
    this.timeOffRequestService = timeOffRequestService;
    this.departmentRepository = departmentRepository;
    this.jobRepository = jobRepository;
    this.employmentTypeRepository = employmentTypeRepository;
    this.officeRepository = officeRepository;
    this.officeAddressRepository = officeAddressRepository;
    this.officeAddressMapper = officeAddressMapper;
    this.officeMapper = officeMapper;
  }

  public JobUser save(final JobUser jobUser) {
    return jobUserRepository.save(jobUser);
  }

  public JobUser getJobUserByUserId(final String userId) {
    return jobUserRepository.findByUserId(userId);
  }

  public void updateJobInfo(final String id, final JobUpdateDto jobUpdateDto,
      final String companyId) {
    final User user = userService.findById(id);
    JobUser jobUser = jobUserRepository.findJobUserByUser(user);
    List<User> users = new ArrayList<>();
    if (jobUser == null) {
      jobUser = new JobUser();
      jobUser.setUser(user);
    } else {
      users = employeeService
          .findDirectReportsByManagerUserId(
              companyId,
              user.getId());
    }
    jobUserMapper.updateFromJobUpdateDto(jobUser, jobUpdateDto);
    jobUserRepository.save(jobUser);

    UserCompensation userCompensation = user.getUserCompensation();

    if (null != userCompensation && jobUpdateDto.getCompensationWage() == null) {
      user.setUserCompensation(new UserCompensation());
    }

    if (jobUpdateDto.getCompensationWage() != null
            && jobUpdateDto.getCompensationFrequencyId() != null) {
      if (null == userCompensation) {
        userCompensation = new UserCompensation();
      }
      userCompensationMapper.updateFromJobUpdateDto(userCompensation, jobUpdateDto);
      userCompensation.setUserId(user.getId());
      userCompensation = userService.saveUserCompensation(userCompensation);
      user.setUserCompensation(userCompensation);
    }

    final String managerId = jobUpdateDto.getManagerId();
    if (!StringUtils.isEmpty(managerId) && (user.getManagerUser() == null
        || !user.getManagerUser().getId().equals(managerId))) {
      final User manager = userService.findById(managerId);
      final Role role = manager.getRole();
      if (Role.EMPLOYEE == role) {
        manager.setUserRole(userRoleService.getManager());
      }
      if (userService.findById(id).getManagerUser() == null) {
        manager.setManagerUser(null);

      } else if (isSubordinate(id, managerId)) {
        manager.setManagerUser(user.getManagerUser());
      }
      user.setManagerUser(manager);
      userService.save(manager);
      final Role userRole = user.getRole();
      if (userRole != Role.ADMIN) {
        users.removeIf(user1 -> user1.getId().equals(managerId));
        final UserRole targetRole = users.isEmpty()
            ? userRoleService.getEmployee() : userRoleService.getManager();
        user.setUserRole(targetRole);
      }
      handlePendingRequests(managerId);
    }
    userService.save(user);
  }

  private boolean isSubordinate(final String userId, String managerId) {
    User user = userService.findById(managerId);
    while (user.getManagerUser() != null && !user.getManagerUser().getId().equals(userId)) {
      managerId = user.getManagerUser().getId();
      user = userService.findById(managerId);
    }
    return user.getManagerUser() != null && user.getManagerUser().getId().equals(userId);
  }

  private void handlePendingRequests(final String userId) {
    final Timestamp startDayTimestamp = DateUtil.getFirstDayOfCurrentYear();
    final String[] timeOffRequestStatuses = new String[]{
            TimeOffApprovalStatus.NO_ACTION.name(),
        TimeOffApprovalStatus.VIEWED.name()};
    final PageRequest request = PageRequest.of(0, 1000);
    final MyTimeOffDto pendingRequests = timeOffRequestService
            .getMyTimeOffRequestsByRequesterUserIdFilteredByStartDay(
            userId, startDayTimestamp, timeOffRequestStatuses, request);
    if (null != pendingRequests && pendingRequests.getTimeOffRequests().hasContent()) {
      final List<TimeOffRequestDto> timeOffRequests = pendingRequests.getTimeOffRequests()
          .getContent();
      final AuthUser authUser = new AuthUser();
      authUser.setId(userId);
      timeOffRequests.stream().forEach(t -> {
        final TimeOffRequestUpdateDto timeOffRequestUpdateDto = new TimeOffRequestUpdateDto();
        timeOffRequestUpdateDto.setStatus(TimeOffApprovalStatus.APPROVED);
        timeOffRequestService.updateTimeOffRequestStatus(
                t.getId(), timeOffRequestUpdateDto, authUser);
      });
    }
  }

  public void updateJobSelectOption(
          final String userId, final JobSelectOptionUpdateDto jobSelectOptionUpdateDto) {
    final String id = jobSelectOptionUpdateDto.getId();
    final String name = jobSelectOptionUpdateDto.getNewName();

    switch (jobSelectOptionUpdateDto.getUpdateField()) {
      case DEPARTMENT:
        updateDepartmentName(id, name);
        break;
      case JOB_TITLE:
        updateJobName(id, name);
        break;
      case EMPLOYMENT_TYPE:
        updateEmployeeTypeName(id, name);
        break;
      case OFFICE_LOCATION:
        updateOfficeName(id, jobSelectOptionUpdateDto.getOfficeCreateDto());
        break;
      default:
        break;
    }

  }

  private void updateDepartmentName(final String id, final String name) {
    final Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    department.setName(name);
    departmentRepository.save(department);
  }

  private void updateJobName(final String id, final String name) {
    final Job job = jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    job.setTitle(name);
    jobRepository.save(job);
  }

  private void updateEmployeeTypeName(final String id, final String name) {
    final EmploymentType employmentType = employmentTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("EmploymentType not found"));
    employmentType.setName(name);
    employmentTypeRepository.save(employmentType);
  }

  private void updateOfficeName(final String id, final OfficeCreateDto officeCreateDto) {
    final Office office = officeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Office not found"));
    office.setName(officeCreateDto.getOfficeName());

    final OfficeAddress officeAddress = officeAddressMapper
            .updateFromOfficeCreateDto(office.getOfficeAddress(), officeCreateDto);

    final Office newOffice = officeMapper.convertToOffice(office, officeCreateDto, officeAddress);
    officeRepository.save(newOffice);
  }

  public void deleteJobSelectOption(
          final String userId, final JobSelectOptionUpdateDto jobSelectOptionUpdateDto) {
    final String id = jobSelectOptionUpdateDto.getId();

    switch (jobSelectOptionUpdateDto.getUpdateField()) {
      case DEPARTMENT:
        deleteDepartmentName(id);
        break;
      case JOB_TITLE:
        deleteJobName(id);
        break;
      case EMPLOYMENT_TYPE:
        deleteEmployeeTypeName(id);
        break;
      case OFFICE_LOCATION:
        deleteOfficeName(id);
        break;
      default:
        break;
    }
  }

  private void deleteDepartmentName(final String id) {
    final Integer count = departmentRepository.findCountByDepartment(id);
    if (count > 0) {
      throw new ForbiddenException(
              "The Department has people, please remove then to another Department");
    }
    final List<Job> jobs = jobRepository.findAllByDepartmentId(id);
    if (!CollectionUtils.isEmpty(jobs)) {
      final List<String> jobIds = jobs.stream().map(Job::getId).collect(Collectors.toList());
      jobRepository.deleteInBatch(jobIds);
    }
    departmentRepository.delete(id);
  }

  private void deleteJobName(final String id) {
    final Integer count = jobUserRepository.getCountByJobId(id);
    if (count > 0) {
      throw new ForbiddenException(
              "The Job has people, please remove then to another Job");
    }
    jobRepository.delete(id);
  }

  private void deleteEmployeeTypeName(final String id) {
    final Integer count = employmentTypeRepository.findCountByType(id);
    if (count > 0) {
      throw new ForbiddenException(
              "The EmployeeType has people, please remove then to another EmployeeType");
    }
    employmentTypeRepository.delete(id);
  }

  private void deleteOfficeName(final String id) {
    final Integer count = officeRepository.findCountByOffice(id);
    if (count > 0) {
      throw new ForbiddenException(
              "The Office has people, please remove then to another Office");
    }
    final Office office = officeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Office not found"));
    officeRepository.delete(id);
    officeAddressRepository.delete(office.getOfficeAddress());
  }
}
