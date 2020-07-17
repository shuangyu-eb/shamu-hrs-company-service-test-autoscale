package shamu.company.job.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.service.DepartmentService;
import shamu.company.common.service.OfficeAddressService;
import shamu.company.common.service.OfficeService;
import shamu.company.company.dto.OfficeCreateDto;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.company.entity.mapper.OfficeAddressMapper;
import shamu.company.company.entity.mapper.OfficeMapper;
import shamu.company.company.entity.mapper.OfficeMapperImpl;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.JobInformationDto;
import shamu.company.job.dto.JobSelectOptionUpdateDto;
import shamu.company.job.dto.JobSelectOptionUpdateField;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.dto.JobUserHireDateCheckDto;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.entity.mapper.JobUserMapperImpl;
import shamu.company.job.exception.errormapping.DeletionFailedCausedByCascadeException;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency.AccrualFrequencyType;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.repository.TimeOffPolicyAccrualScheduleRepository;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.EmployeeType;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.service.CompensationOvertimeStatusService;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;
import shamu.company.utils.UuidUtil;

class JobUserServiceTests {

  private final UserCompensationMapper userCompensationMapper =
      Mappers.getMapper(UserCompensationMapper.class);
  private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
  @Mock private JobUserRepository jobUserRepository;
  @Mock private UserService userService;
  @Mock private UserRoleService userRoleService;
  @Mock private TimeOffRequestService timeOffRequestService;
  @Mock private DepartmentService departmentService;
  @Mock private JobService jobService;
  @Mock private OfficeService officeService;
  @Mock private OfficeAddressService officeAddressService;
  @Mock private OfficeAddressMapper officeAddressMapper;
  @Mock private TimeOffPolicyUserRepository timeOffPolicyUserRepository;
  @Mock private TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository;
  @Mock private TimeOffPolicyService timeOffPolicyService;
  @Mock private UserAddressRepository userAddressRepository;
  @Mock private UserAddressMapper userAddressMapper;
  private final OfficeMapper officeMapper = new OfficeMapperImpl(officeAddressMapper);
  private final JobUserMapper jobUserMapper =
      new JobUserMapperImpl(officeMapper, userCompensationMapper);
  private JobUserService jobUserService;
  private CompensationOvertimeStatusService compensationOvertimeStatusService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    jobUserService =
        new JobUserService(
            jobUserRepository,
            userService,
            jobUserMapper,
            userCompensationMapper,
            userRoleService,
            timeOffRequestService,
            departmentService,
            officeService,
            officeAddressService,
            officeAddressMapper,
            officeMapper,
            jobService,
            userMapper,
            timeOffPolicyUserRepository,
            timeOffPolicyAccrualScheduleRepository,
            userAddressRepository,
            userAddressMapper,
            timeOffPolicyService,
            compensationOvertimeStatusService);
  }

  @Test
  void save() {
    final JobUser jobUser = new JobUser();
    Mockito.when(jobUserService.save(jobUser)).thenReturn(jobUser);
    Assertions.assertEquals(jobUserService.save(jobUser), jobUser);
    Mockito.verify(jobUserRepository, Mockito.times(1)).save(Mockito.any());
  }

  @Test
  void findJobsByDepartmentId() {
    final String id = "1";
    final Job job1 = new Job();
    job1.setId("1");
    final List<Job> jobs = new LinkedList<>();
    jobs.add(job1);

    Mockito.when(jobService.findAllByCompanyId(id)).thenReturn(jobs);
    Mockito.when(jobUserService.getCountByJobId(job1.getId())).thenReturn(2);
    Assertions.assertDoesNotThrow(() -> jobUserService.findJobsByCompanyId(id));
  }

  @Test
  void findHomeAndOfficeAddressByUsers() {
    final String userId = "userId";
    final List userIds = new ArrayList();
    userIds.add(userId);
    Assertions.assertDoesNotThrow(() -> jobUserService.findHomeAndOfficeAddressByUsers(userIds));
  }

  @Nested
  class updateJobInfo {

    JobUpdateDto jobUpdateDto = new JobUpdateDto();

    User manager = new User();

    User user = new User();

    List<User> subordinates = new ArrayList<>();

    JobUser jobUser = new JobUser();

    @BeforeEach
    void init() {
      jobUpdateDto.setJobId("1");
      jobUpdateDto.setCompensationFrequencyId("1");
      jobUpdateDto.setCompensationWage(1000.00);
      jobUpdateDto.setManagerId("2");
      jobUpdateDto.setStartDate(DateUtil.getToday());

      manager.setId("2");
      final UserRole managerRole = new UserRole();
      managerRole.setName(User.Role.MANAGER.name());
      manager.setUserRole(managerRole);

      user.setId("1");
      final UserRole userRole = new UserRole();
      userRole.setName(User.Role.EMPLOYEE.name());
      user.setUserRole(userRole);
      user.setManagerUser(manager);

      jobUser.setId("1");
      jobUser.setUser(user);

      subordinates.add(user);
      subordinates.add(manager);
    }

    @Test
    void WhenManagerIdIsEmpty_thenUserShouldNotBeUpdated() {
      Mockito.when(userService.findById(user.getId())).thenReturn(user);
      Mockito.when(jobUserRepository.findJobUserByUser(jobUser.getUser())).thenReturn(jobUser);
      Mockito.when(jobUserRepository.save(jobUser)).thenReturn(jobUser);
      jobUpdateDto.setManagerId("");

      Assertions.assertDoesNotThrow(() -> jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
      Mockito.verify(userService, Mockito.times(0))
          .findSubordinatesByManagerUserId(Mockito.any(), Mockito.any());
      Mockito.verify(userService, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void WhenJobUserIsNull_thenShouldCreateNewJobUser() {
      Mockito.when(userService.findById(user.getId())).thenReturn(user);
      Mockito.when(jobUserRepository.findJobUserByUser(user)).thenReturn(null);
      Mockito.when(jobUserRepository.save(jobUser)).thenReturn(jobUser);
      jobUpdateDto.setManagerId("");

      Assertions.assertDoesNotThrow(() -> jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
      Assertions.assertNull(jobUserService.findJobUserByUser(user));
      Mockito.verify(userService, Mockito.times(0))
          .findSubordinatesByManagerUserId(Mockito.any(), Mockito.any());
      Mockito.verify(userService, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void WhenManagerIsNotChanged_thenShouldSuccess() {

      Mockito.when(userService.findById(user.getId())).thenReturn(user);
      Mockito.when(jobUserRepository.findJobUserByUser(jobUser.getUser())).thenReturn(jobUser);
      Mockito.when(userService.findById(manager.getId())).thenReturn(manager);
      Mockito.when(userService.findSubordinatesByManagerUserId(Mockito.any(), Mockito.any()))
          .thenReturn(subordinates);
      Mockito.when(jobUserRepository.save(jobUser)).thenReturn(jobUser);
      Mockito.when(userService.save(manager)).thenReturn(manager);
      Mockito.when(userService.save(user)).thenReturn(user);

      Assertions.assertDoesNotThrow(() -> jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
      Assertions.assertEquals(manager.getUserRole(), user.getManagerUser().getUserRole());
      Mockito.verify(userService, Mockito.times(0))
          .findSubordinatesByManagerUserId(Mockito.any(), Mockito.any());
      Mockito.verify(userService, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void WhenManagerIsChanged_isSubordinateFalse_thenShouldSuccess() {
      jobUpdateDto.setManagerId("3");

      final User oldManager = new User();
      oldManager.setId("2");
      final UserRole managerRole = new UserRole();
      managerRole.setName(User.Role.MANAGER.name());
      oldManager.setUserRole(managerRole);

      user.setManagerUser(oldManager);

      final User newManager = new User();
      newManager.setId("3");
      final UserRole newManagerRole = new UserRole();
      newManagerRole.setName(User.Role.EMPLOYEE.name());
      newManager.setUserRole(newManagerRole);

      final List<User> subordinatesList = new ArrayList<>();
      subordinatesList.add(user);
      subordinatesList.add(oldManager);

      Mockito.when(userService.findById(user.getId())).thenReturn(user);
      Mockito.when(jobUserRepository.findJobUserByUser(jobUser.getUser())).thenReturn(jobUser);
      Mockito.when(userService.findById(oldManager.getId())).thenReturn(oldManager);
      Mockito.when(userService.findById(newManager.getId())).thenReturn(newManager);
      Mockito.when(userService.findSubordinatesByManagerUserId(Mockito.any(), Mockito.any()))
          .thenReturn(subordinatesList);
      Mockito.when(jobUserRepository.save(jobUser)).thenReturn(jobUser);
      Mockito.when(userService.save(oldManager)).thenReturn(oldManager);
      Mockito.when(userService.save(user)).thenReturn(user);

      Assertions.assertDoesNotThrow(() -> jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
      Assertions.assertEquals(jobUpdateDto.getManagerId(), user.getManagerUser().getId());
      Mockito.verify(userService, Mockito.times(1))
          .findSubordinatesByManagerUserId(Mockito.any(), Mockito.any());
      Mockito.verify(userService, Mockito.times(2)).save(Mockito.any());
    }

    @Test
    void WhenManagerIsChanged_isSubordinateTrue_thenShouldSuccess() {
      jobUpdateDto.setManagerId("3");

      final User oldManager = new User();
      oldManager.setId("2");
      final UserRole managerRole = new UserRole();
      managerRole.setName(User.Role.MANAGER.name());
      oldManager.setUserRole(managerRole);

      user.setManagerUser(oldManager);

      final User subordinate = new User();
      subordinate.setId("4");
      subordinate.setManagerUser(user);

      final User newManager = new User();
      newManager.setId("3");
      final UserRole newManagerRole = new UserRole();
      newManagerRole.setName(User.Role.EMPLOYEE.name());
      newManager.setUserRole(newManagerRole);
      newManager.setManagerUser(subordinate);

      final List<User> subordinatesList = new ArrayList<>();
      subordinatesList.add(user);
      subordinatesList.add(oldManager);
      subordinatesList.add(subordinate);

      Mockito.when(userService.findById(user.getId())).thenReturn(user);
      Mockito.when(jobUserRepository.findJobUserByUser(jobUser.getUser())).thenReturn(jobUser);
      Mockito.when(userService.findById(oldManager.getId())).thenReturn(oldManager);
      Mockito.when(userService.findById(newManager.getId())).thenReturn(newManager);
      Mockito.when(userService.findById(subordinate.getId())).thenReturn(subordinate);
      Mockito.when(userService.findSubordinatesByManagerUserId(Mockito.any(), Mockito.any()))
          .thenReturn(subordinatesList);
      Mockito.when(jobUserRepository.save(jobUser)).thenReturn(jobUser);
      Mockito.when(userService.save(oldManager)).thenReturn(oldManager);
      Mockito.when(userService.save(user)).thenReturn(user);

      Assertions.assertDoesNotThrow(() -> jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
      Assertions.assertEquals(jobUpdateDto.getManagerId(), user.getManagerUser().getId());
      Assertions.assertEquals(oldManager.getId(), user.getManagerUser().getManagerUser().getId());
      Mockito.verify(userService, Mockito.times(1))
          .findSubordinatesByManagerUserId(Mockito.any(), Mockito.any());
      Mockito.verify(userService, Mockito.times(2)).save(Mockito.any());
    }

    @Test
    void WhenManagerIdIsStringNull_thenUserManagerIsNullAndSuccess() {
      jobUpdateDto.setManagerId(null);

      Mockito.when(userService.findById(user.getId())).thenReturn(user);
      Mockito.when(jobUserRepository.findJobUserByUser(jobUser.getUser())).thenReturn(jobUser);
      Mockito.when(jobUserRepository.save(jobUser)).thenReturn(jobUser);

      Assertions.assertDoesNotThrow(() -> jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
      Mockito.verify(userService, Mockito.times(0))
          .findSubordinatesByManagerUserId(Mockito.any(), Mockito.any());
      Mockito.verify(userService, Mockito.times(1)).save(Mockito.any());
    }
  }

  @Nested
  class checkJobInfoComplete {


    @Test
    void whenJobUserIsNull_thenReturnFalse() {
      final String userId = UuidUtil.getUuidString();
      Mockito.when(jobUserRepository.findByUserId(userId)).thenReturn(null);
      Assertions.assertFalse(jobUserService.checkJobInfoComplete(userId));
    }

    @Test
    void whenEmployeeTypeIsNull_thenReturnFalse() {
      final JobUser jobUser = new JobUser();
      final String userId = UuidUtil.getUuidString();
      jobUser.setEmployeeType(null);
      Mockito.when(jobUserRepository.findByUserId(userId)).thenReturn(jobUser);
      Assertions.assertFalse(jobUserService.checkJobInfoComplete(userId));
    }

    @Test
    void whenJobInfoIsComplete_thenReturnTrue() {
      final JobUser jobUser = new JobUser();
      final String userId = UuidUtil.getUuidString();
      final EmployeeType employeeType = new EmployeeType();
      employeeType.setName("test");
      jobUser.setEmployeeType(employeeType);
      jobUser.setStartDate(new Timestamp(123));
      final Office office = new Office();
      office.setId("1");
      office.setOfficeAddress(new OfficeAddress());
      office.setCompany(new Company(UuidUtil.getUuidString()));
      jobUser.setOffice(office);
      UserCompensation userCompensation = new UserCompensation();
      jobUser.setUserCompensation(userCompensation);
      Mockito.when(jobUserRepository.findByUserId(userId)).thenReturn(jobUser);
      Assertions.assertTrue(jobUserService.checkJobInfoComplete(userId));
    }

    @Test
    void whenUserAddressInfoIsNotComplete_thenReturnFalse() {
      final JobUser jobUser = new JobUser();
      final String userId = UuidUtil.getUuidString();
      final EmployeeType employeeType = new EmployeeType();
      employeeType.setName("test");
      jobUser.setEmployeeType(employeeType);
      jobUser.setStartDate(null);
      jobUser.setOffice(null);
      jobUser.setUserCompensation(null);
      Mockito.when(jobUserRepository.findByUserId(userId)).thenReturn(jobUser);
      Assertions.assertFalse(jobUserService.checkJobInfoComplete(userId));
    }
  }

  @Nested
  class FindJobMessage {

    private String targetUserId;
    private String userId;
    private JobUser jobUser;
    private User currentUser;

    @BeforeEach
    void init() {
      jobUser = new JobUser();
      final User targetUser = new User();
      targetUserId = RandomStringUtils.randomAlphabetic(16);
      targetUser.setId(targetUserId);
      final UserRole userRole = new UserRole();
      userRole.setName(Role.MANAGER.name());
      targetUser.setUserRole(userRole);
      jobUser.setUser(targetUser);
      Mockito.when(jobUserService.getJobUserByUserId(Mockito.anyString())).thenReturn(jobUser);
      Mockito.when(userService.findById(targetUserId)).thenReturn(targetUser);

      currentUser = new User();
      userId = RandomStringUtils.randomAlphabetic(16);
      currentUser.setId(userId);
      final UserRole currentUserRole = new UserRole();
      currentUserRole.setName(Role.MANAGER.name());
      currentUser.setUserRole(currentUserRole);
      Mockito.when(userService.findById(userId)).thenReturn(currentUser);
    }

    @Test
    void whenCanNotFindUserJob_thenReturnBasicJobInformation() {
      Mockito.when(jobUserService.getJobUserByUserId(Mockito.anyString())).thenReturn(null);
      final BasicJobInformationDto jobInformation =
          jobUserService.findJobMessage(targetUserId, userId);
      Assertions.assertNotNull(jobInformation);
    }

    @Test
    void whenIsCurrentUser_thenReturnJobInformation() {
      final BasicJobInformationDto jobInformation =
          jobUserService.findJobMessage(targetUserId, targetUserId);
      Assertions.assertTrue(jobInformation instanceof JobInformationDto);
    }

    @Test
    void whenIsAdmin_thenReturnJobInformation() {
      final UserRole adminRole = new UserRole();
      adminRole.setName(Role.ADMIN.name());
      currentUser.setUserRole(adminRole);
      final BasicJobInformationDto jobInformation =
          jobUserService.findJobMessage(targetUserId, userId);
      Assertions.assertTrue(jobInformation instanceof JobInformationDto);
    }

    @Test
    void whenIsUserManager_thenReturnJobInformation() {
      jobUser.getUser().setManagerUser(currentUser);
      final BasicJobInformationDto jobInformation =
          jobUserService.findJobMessage(targetUserId, userId);
      Assertions.assertTrue(jobInformation instanceof JobInformationDto);
    }

    @Test
    void whenIsEmployee_thenReturnBasicJobInformation() {
      final User randomManagerUser = new User();
      jobUser.getUser().setManagerUser(currentUser);
      randomManagerUser.setId(RandomStringUtils.randomAlphabetic(16));
      final BasicJobInformationDto jobInformation =
          jobUserService.findJobMessage(targetUserId, userId);
      Assertions.assertNotNull(jobInformation);
    }
  }

  @Nested
  class updateJobSelectOption {
    private JobSelectOptionUpdateDto jobSelectOptionUpdateDto;

    @BeforeEach
    void init() {
      jobSelectOptionUpdateDto = new JobSelectOptionUpdateDto();
      jobSelectOptionUpdateDto.setId("1");
      jobSelectOptionUpdateDto.setNewName("new name");
    }

    @Test
    void whenUpdatedFieldIsJobTitle_thenUpdateShouldSuccess() {
      jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.JOB_TITLE);
      final Job job = new Job();
      job.setId("1");
      job.setCompany(new Company(UuidUtil.getUuidString()));

      Mockito.when(jobService.findById(job.getId())).thenReturn(job);
      Mockito.when(
              jobService.findByTitleAndCompanyId(
                  jobSelectOptionUpdateDto.getNewName(), job.getCompany().getId()))
          .thenReturn(Collections.emptyList());
      Mockito.when(jobService.save(job)).thenReturn(job);
      Assertions.assertDoesNotThrow(
          () -> jobUserService.updateJobSelectOption(jobSelectOptionUpdateDto));
      Mockito.verify(jobService, Mockito.times(1)).save(Mockito.any());
      Mockito.verify(jobService, Mockito.times(1)).findById(Mockito.any());
    }

    @Test
    void whenUpdatedFieldIsOfficeLocation_thenUpdateShouldSuccess() {
      jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.OFFICE_LOCATION);
      final Office office = new Office();
      office.setId("1");
      office.setOfficeAddress(new OfficeAddress());
      office.setCompany(new Company(UuidUtil.getUuidString()));

      final OfficeCreateDto officeCreateDto = new OfficeCreateDto();
      officeCreateDto.setOfficeName("office name");
      officeCreateDto.setCity("city");
      officeCreateDto.setStateId("1");
      officeCreateDto.setStreet1("s1");
      officeCreateDto.setStreet2("s2");
      officeCreateDto.setPostalCode("postalCode");
      jobSelectOptionUpdateDto.setOfficeCreateDto(officeCreateDto);

      final OfficeAddress officeAddress =
          officeAddressMapper.updateFromOfficeCreateDto(office.getOfficeAddress(), officeCreateDto);

      final Office newOffice = officeMapper.convertToOffice(office, officeCreateDto, officeAddress);

      Mockito.when(officeService.findById(office.getId())).thenReturn(office);

      Mockito.when(
              officeService.findByNameAndCompanyId(
                  officeCreateDto.getOfficeName(), office.getCompany().getId()))
          .thenReturn(Collections.emptyList());
      Mockito.when(officeService.save(newOffice)).thenReturn(newOffice);
      Assertions.assertDoesNotThrow(
          () -> jobUserService.updateJobSelectOption(jobSelectOptionUpdateDto));
      Mockito.verify(officeService, Mockito.times(1)).save(Mockito.any());
      Mockito.verify(officeService, Mockito.times(1)).findById(Mockito.any());
    }

    @Test
    void whenUpdatedFieldIsDepartment_thenUpdateShouldSuccess() {
      jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.DEPARTMENT);
      final Department department = new Department();
      department.setId("1");
      department.setCompany(new Company(UuidUtil.getUuidString()));

      Mockito.when(departmentService.findById(department.getId())).thenReturn(department);
      Mockito.when(
              departmentService.findByNameAndCompanyId(
                  jobSelectOptionUpdateDto.getNewName(), department.getCompany().getId()))
          .thenReturn(Collections.emptyList());
      Mockito.when(departmentService.save(department)).thenReturn(department);
      Assertions.assertDoesNotThrow(
          () -> jobUserService.updateJobSelectOption(jobSelectOptionUpdateDto));
      Mockito.verify(departmentService, Mockito.times(1)).save(Mockito.any());
      Mockito.verify(departmentService, Mockito.times(1)).findById(Mockito.any());
    }
  }

  @Nested
  class deleteJobSelectOption {
    private JobSelectOptionUpdateDto jobSelectOptionUpdateDto;

    @BeforeEach
    void init() {
      jobSelectOptionUpdateDto = new JobSelectOptionUpdateDto();
      jobSelectOptionUpdateDto.setId("1");
    }

    @Test
    void whenDepartmentHasEmployee_thenDeleteDepartmentShouldFail() {
      jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.DEPARTMENT);
      Mockito.when(departmentService.findCountByDepartment(jobSelectOptionUpdateDto.getId()))
          .thenReturn(10);
      Assertions.assertThrows(
          DeletionFailedCausedByCascadeException.class,
          () -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
      Mockito.verify(departmentService, Mockito.times(0)).delete(Mockito.any());
      Mockito.verify(jobService, Mockito.times(0)).findAllByCompanyId(Mockito.any());
      Mockito.verify(jobService, Mockito.times(0)).deleteInBatch(Mockito.any());
    }

    @Test
    void whenDepartmentHasNoEmployee_departmentHasNoJobs_thenDeleteDepartmentShouldSuccess() {
      jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.DEPARTMENT);
      Mockito.when(departmentService.findCountByDepartment(jobSelectOptionUpdateDto.getId()))
          .thenReturn(0);
      Mockito.when(jobService.findAllByCompanyId(jobSelectOptionUpdateDto.getId()))
          .thenReturn(Collections.emptyList());
      Assertions.assertDoesNotThrow(
          () -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
      Mockito.verify(departmentService, Mockito.times(1)).delete(Mockito.any());
    }

    @Test
    void whenDepartmentHasNoEmployee_departmentHasJobs_thenDeleteDepartmentShouldSuccess() {
      jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.DEPARTMENT);
      final Job job = new Job();
      job.setId("1");
      final List<Job> jobs = new LinkedList<>();
      jobs.add(job);

      Mockito.when(departmentService.findCountByDepartment(jobSelectOptionUpdateDto.getId()))
          .thenReturn(0);
      Mockito.when(jobService.findAllByCompanyId(jobSelectOptionUpdateDto.getId()))
          .thenReturn(jobs);
      Assertions.assertDoesNotThrow(
          () -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
      Mockito.verify(departmentService, Mockito.times(1)).delete(Mockito.any());
    }

    @Test
    void whenEmployeeBelongToThisJobTitle_thenDeleteJobTitleShouldFail() {
      jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.JOB_TITLE);
      Mockito.when(jobUserService.getCountByJobId(jobSelectOptionUpdateDto.getId())).thenReturn(10);
      Assertions.assertThrows(
          DeletionFailedCausedByCascadeException.class,
          () -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
      Mockito.verify(jobService, Mockito.times(0)).delete(Mockito.any());
    }

    @Test
    void whenNoEmployeeBelongsToThisJobTitle_thenDeleteJobTitleShouldSuccess() {
      jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.JOB_TITLE);
      Mockito.when(departmentService.findCountByDepartment(jobSelectOptionUpdateDto.getId()))
          .thenReturn(0);
      Assertions.assertDoesNotThrow(
          () -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
      Mockito.verify(jobService, Mockito.times(1)).delete(Mockito.any());
    }

    @Test
    void whenEmployeeBelongToThisOffice_thenDeleteOfficeShouldFail() {
      jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.OFFICE_LOCATION);
      Mockito.when(officeService.findCountByOffice(jobSelectOptionUpdateDto.getId()))
          .thenReturn(10);
      Assertions.assertThrows(
          DeletionFailedCausedByCascadeException.class,
          () -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
      Mockito.verify(officeService, Mockito.times(0)).delete(Mockito.any());
      Mockito.verify(officeAddressService, Mockito.times(0)).delete(Mockito.any());
    }

    @Test
    void whenNoEmployeeBelongsToThisOffice_thenDeleteOfficeShouldSuccess() {
      jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.OFFICE_LOCATION);
      final Office office = new Office();
      final OfficeAddress officeAddress = new OfficeAddress();
      office.setOfficeAddress(officeAddress);

      Mockito.when(officeService.findCountByOffice(jobSelectOptionUpdateDto.getId())).thenReturn(0);
      Mockito.when(officeService.findById(jobSelectOptionUpdateDto.getId())).thenReturn(office);
      Assertions.assertDoesNotThrow(
          () -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
      Mockito.verify(officeService, Mockito.times(1)).delete(Mockito.any());
      Mockito.verify(officeAddressService, Mockito.times(1)).delete(Mockito.any());
    }
  }

  @Nested
  class checkUserHireDateDeletable {
    private JobUserHireDateCheckDto jobUserHireDateCheckDto;
    private User user;
    private JobUser jobUser;
    private TimeOffPolicyUser timeOffPolicyUser;

    @BeforeEach
    void init() {
      jobUserHireDateCheckDto = new JobUserHireDateCheckDto();
      user = new User("1");
      jobUser = new JobUser();
    }

    @Test
    void whenUserHasNoTimeOffPolicy_thenHireDateCanBeDeleted() {
      jobUserHireDateCheckDto.setHireDateDeletable(true);
      jobUser.setStartDate(null);

      Mockito.when(jobUserRepository.findJobUserByUser(Mockito.any())).thenReturn(jobUser);

      Assertions.assertEquals(
          jobUserHireDateCheckDto.getHireDateDeletable(),
          jobUserService.checkUserHireDateDeletable("1").getHireDateDeletable());
    }

    @Test
    void whenUserHasNoPolicyRelatedToHireDate_thenHireDateCanBeDeleted() {
      jobUserHireDateCheckDto.setHireDateDeletable(true);
      jobUser.setStartDate(new Timestamp(123));
      final List<TimeOffPolicyUser> timeOffPolicyUsers = new ArrayList<>();
      final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
      final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
      timeOffPolicy.setIsLimited(false);
      timeOffPolicyUser.setTimeOffPolicy(timeOffPolicy);
      timeOffPolicyUsers.add(timeOffPolicyUser);
      final TimeOffPolicyUser timeOffPolicyUser2 = new TimeOffPolicyUser();
      final TimeOffPolicy timeOffPolicy2 = new TimeOffPolicy();
      timeOffPolicy2.setIsLimited(true);
      timeOffPolicyUser2.setTimeOffPolicy(timeOffPolicy2);
      timeOffPolicyUsers.add(timeOffPolicyUser2);

      final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
          new TimeOffPolicyAccrualSchedule();
      final TimeOffAccrualFrequency timeOffAccrualFrequency = new TimeOffAccrualFrequency();
      timeOffAccrualFrequency.setName(AccrualFrequencyType.FREQUENCY_TYPE_ONE.getValue());
      timeOffPolicyAccrualSchedule.setTimeOffAccrualFrequency(timeOffAccrualFrequency);

      Mockito.when(jobUserRepository.findJobUserByUser(Mockito.any())).thenReturn(jobUser);
      Mockito.when(timeOffPolicyUserRepository.findTimeOffPolicyUsersByUser(Mockito.any()))
          .thenReturn(timeOffPolicyUsers);
      Mockito.when(timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(Mockito.any()))
          .thenReturn(timeOffPolicyAccrualSchedule);

      Assertions.assertEquals(
          jobUserHireDateCheckDto.getHireDateDeletable(),
          jobUserService.checkUserHireDateDeletable("1").getHireDateDeletable());
    }

    @Test
    void whenUserHasPolicyRelatedToHireDate_thenHireDateCanNotBeDeleted() {
      jobUserHireDateCheckDto.setHireDateDeletable(false);
      jobUser.setStartDate(new Timestamp(123));
      final List<TimeOffPolicyUser> timeOffPolicyUsers = new ArrayList<>();
      final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
      final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
      timeOffPolicy.setIsLimited(true);
      timeOffPolicyUser.setTimeOffPolicy(timeOffPolicy);
      timeOffPolicyUsers.add(timeOffPolicyUser);
      final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
          new TimeOffPolicyAccrualSchedule();
      final TimeOffAccrualFrequency timeOffAccrualFrequency = new TimeOffAccrualFrequency();
      timeOffAccrualFrequency.setName(AccrualFrequencyType.FREQUENCY_TYPE_TWO.getValue());
      timeOffPolicyAccrualSchedule.setTimeOffAccrualFrequency(timeOffAccrualFrequency);

      Mockito.when(jobUserRepository.findJobUserByUser(Mockito.any())).thenReturn(jobUser);
      Mockito.when(timeOffPolicyUserRepository.findTimeOffPolicyUsersByUser(Mockito.any()))
          .thenReturn(timeOffPolicyUsers);
      Mockito.when(timeOffPolicyAccrualScheduleRepository.findByTimeOffPolicy(Mockito.any()))
          .thenReturn(timeOffPolicyAccrualSchedule);
      Mockito.when(timeOffPolicyService.checkIsPolicyCalculationRelatedToHireDate(Mockito.any()))
          .thenReturn(true);

      Assertions.assertEquals(
          jobUserHireDateCheckDto.getHireDateDeletable(),
          jobUserService.checkUserHireDateDeletable("1").getHireDateDeletable());
    }
  }
}
