package shamu.company.job.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.StateProvince;
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
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.dto.JobSelectOptionUpdateDto;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobRepository;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.server.AuthUser;
import shamu.company.timeoff.dto.MyTimeOffDto;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.dto.TimeOffRequestUpdateDto;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
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
      final OfficeAddressRepository officeAddressRepository) {
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
  }

  public JobUser getJobUserByUserId(final Long userId) {
    return jobUserRepository.findByUserId(userId);
  }

  public void updateJobInfo(final Long id, final JobUpdateDto jobUpdateDto, final Long companyId) {
    final User user = userService.findUserById(id);
    JobUser jobUser = jobUserRepository.findJobUserByUser(user);
    List<User> users = new ArrayList<>();
    if (jobUser == null) {
      jobUser = new JobUser();
      jobUser.setUser(user);
    } else {
      users = employeeService
          .findDirectReportsEmployersAndEmployeesByCompanyId(
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

    final Long managerId = jobUpdateDto.getManagerId();
    if (managerId != null && (user.getManagerUser() == null
        || !user.getManagerUser().getId().equals(managerId))) {
      final User manager = userService.findUserById(managerId);
      final Role role = manager.getRole();
      if (Role.EMPLOYEE == role) {
        manager.setUserRole(userRoleService.getManager());
      }
      if (userService.findUserById(id).getManagerUser() == null) {
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

  private boolean isSubordinate(final Long userId, Long managerId) {
    User user = userService.findUserById(managerId);
    while (user.getManagerUser() != null && !user.getManagerUser().getId().equals(userId)) {
      managerId = user.getManagerUser().getId();
      user = userService.findUserById(managerId);
    }
    return user.getManagerUser() != null && user.getManagerUser().getId().equals(userId);
  }

  private void handlePendingRequests(Long userId) {
    final Timestamp startDayTimestamp = DateUtil.getFirstDayOfCurrentYear();
    final Long[] timeOffRequestStatuses = new Long[]{
            TimeOffRequestApprovalStatus.NO_ACTION.getValue(),
            TimeOffRequestApprovalStatus.VIEWED.getValue()};
    final PageRequest request = PageRequest.of(0, 1000);
    MyTimeOffDto pendingRequests = timeOffRequestService
            .getMyTimeOffRequestsByRequesterUserIdFilteredByStartDay(
            userId, startDayTimestamp, timeOffRequestStatuses, request);
    if (null != pendingRequests && pendingRequests.getTimeOffRequests().hasContent()) {
      List<TimeOffRequestDto> timeOffRequests = pendingRequests.getTimeOffRequests().getContent();
      AuthUser authUser = new AuthUser();
      authUser.setId(userId);
      timeOffRequests.stream().forEach(t -> {
        TimeOffRequestUpdateDto timeOffRequestUpdateDto = new TimeOffRequestUpdateDto();
        timeOffRequestUpdateDto.setStatus(TimeOffRequestApprovalStatus.APPROVED);
        timeOffRequestService.updateTimeOffRequestStatus(
                t.getId(), timeOffRequestUpdateDto, authUser);
      });
    }
  }

  public void updateJobSelectOption(
          final Long userId, final JobSelectOptionUpdateDto jobSelectOptionUpdateDto) {
    Long id = jobSelectOptionUpdateDto.getId();
    String name = jobSelectOptionUpdateDto.getNewName();

    switch (jobSelectOptionUpdateDto.getUpdateField()) {
      case "Department ":
        updateDepartmentName(id, name);
        break;
      case "Job Title ":
        updateJobName(id, name);
        break;
      case "Employment Type":
        updateEmployeeTypeName(id, name);
        break;
      case "Office Location":
        updateOfficeName(id, jobSelectOptionUpdateDto.getOfficeCreateDto());
        break;
      default:
        break;
    }

  }

  private void updateDepartmentName(Long id, String name) {
    Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    department.setName(name);
    departmentRepository.save(department);
  }

  private void updateJobName(Long id, String name) {
    Job job = jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    job.setTitle(name);
    jobRepository.save(job);
  }

  private void updateEmployeeTypeName(Long id, String name) {
    EmploymentType employmentType = employmentTypeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("EmploymentType not found"));
    employmentType.setName(name);
    employmentTypeRepository.save(employmentType);
  }

  private void updateOfficeName(Long id, OfficeCreateDto officeCreateDto) {
    Office office = officeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Office not found"));
    office.setName(officeCreateDto.getOfficeName());

    OfficeAddress officeAddress = office.getOfficeAddress();
    officeAddress.setStreet1(officeCreateDto.getStreet1());
    officeAddress.setStreet2(officeCreateDto.getStreet2());
    officeAddress.setCity(officeCreateDto.getCity());
    if (null != officeCreateDto.getStateId()) {
      officeAddress.setStateProvince(new StateProvince(officeCreateDto.getStateId()));
    }
    officeAddress.setPostalCode(officeCreateDto.getZip());
    OfficeAddress updateOfficeAddress = officeAddressRepository.save(officeAddress);

    office.setOfficeAddress(updateOfficeAddress);
    officeRepository.save(office);
  }

  public void deleteJobSelectOption(
          final Long userId, final JobSelectOptionUpdateDto jobSelectOptionUpdateDto) {
    Long id = jobSelectOptionUpdateDto.getId();

    switch (jobSelectOptionUpdateDto.getUpdateField()) {
      case "Department ":
        deleteDepartmentName(id);
        break;
      case "Job Title ":
        deleteJobName(id);
        break;
      case "Employment Type":
        deleteEmployeeTypeName(id);
        break;
      case "Office Location":
        deleteOfficeName(id);
        break;
      default:
        break;
    }
  }

  private void deleteDepartmentName(Long id) {
    Integer count = departmentRepository.getCountByDepartment(id);
    if (count > 0) {
      throw new ForbiddenException(
              "The Department has people, please remove then to another Department");
    }
    departmentRepository.delete(id);
  }

  private void deleteJobName(Long id) {
    Integer count = jobUserRepository.getCountByJobId(id);
    if (count > 0) {
      throw new ForbiddenException(
              "The Job has people, please remove then to another Job");
    }
    jobRepository.delete(id);
  }

  private void deleteEmployeeTypeName(Long id) {
    Integer count = employmentTypeRepository.getCountByType(id);
    if (count > 0) {
      throw new ForbiddenException(
              "The EmployeeType has people, please remove then to another EmployeeType");
    }
    employmentTypeRepository.delete(id);
  }

  private void deleteOfficeName(Long id) {
    Integer count = officeRepository.getCountByOffice(id);
    if (count > 0) {
      throw new ForbiddenException(
              "The Office has people, please remove then to another Office");
    }
    Office office = officeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Office not found"));
    officeRepository.delete(id);
    officeAddressRepository.delete(office.getOfficeAddress());
  }
}
