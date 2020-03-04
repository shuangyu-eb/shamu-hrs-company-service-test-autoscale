package shamu.company.job.service;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.service.DepartmentService;
import shamu.company.common.service.OfficeAddressService;
import shamu.company.common.service.OfficeService;
import shamu.company.company.dto.OfficeCreateDto;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.company.entity.mapper.OfficeAddressMapper;
import shamu.company.company.entity.mapper.OfficeMapper;
import shamu.company.company.entity.mapper.OfficeMapperImpl;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.JobInformationDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmploymentTypeService;
import shamu.company.job.dto.JobSelectOptionUpdateDto;
import shamu.company.job.dto.JobSelectOptionUpdateField;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.entity.mapper.JobUserMapperImpl;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class JobUserServiceTests {

    @Mock
    private JobUserRepository jobUserRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private TimeOffRequestService timeOffRequestService;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private JobService jobService;

    @Mock
    private EmploymentTypeService employmentTypeService;

    @Mock
    private OfficeService officeService;

    @Mock
    private OfficeAddressService officeAddressService;

    @Mock
    private OfficeAddressMapper officeAddressMapper;

    private final OfficeMapper officeMapper = new OfficeMapperImpl(officeAddressMapper);

    private final UserCompensationMapper userCompensationMapper = Mappers.getMapper(UserCompensationMapper.class);

    private final JobUserMapper jobUserMapper =
        new JobUserMapperImpl(officeMapper, userCompensationMapper);

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private JobUserService jobUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        jobUserService = new JobUserService(jobUserRepository, userService, jobUserMapper,
            userCompensationMapper, userRoleService, timeOffRequestService, departmentService,
            employmentTypeService, officeService, officeAddressService, officeAddressMapper,
            officeMapper, jobService, userMapper);
    }

    @Test
    void save () {
      JobUser jobUser = new JobUser();
      Mockito.when(jobUserService.save(jobUser)).thenReturn(jobUser);
      Assertions.assertEquals(jobUserService.save(jobUser), jobUser);
      Mockito.verify(jobUserRepository, Mockito.times(1)).save(Mockito.any());
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
            jobUpdateDto.setCompensationWage(1000.0);
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

          Assertions.assertDoesNotThrow(() ->
              jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
          Mockito.verify(userService, Mockito.times( 0)).findSubordinatesByManagerUserId(Mockito.any(), Mockito.any());
          Mockito.verify(userService, Mockito.times(0)).save(Mockito.any());
        }

        @Test
        void WhenJobUserIsNull_thenShouldCreateNewJobUser() {
          Mockito.when(userService.findById(user.getId())).thenReturn(user);
          Mockito.when(jobUserRepository.findJobUserByUser(user)).thenReturn(null);
          Mockito.when(jobUserRepository.save(jobUser)).thenReturn(jobUser);
          jobUpdateDto.setManagerId("");

          Assertions.assertDoesNotThrow(() ->
              jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
          Assertions.assertNull(jobUserService.findJobUserByUser(user));
          Mockito.verify(userService, Mockito.times( 0)).findSubordinatesByManagerUserId(Mockito.any(), Mockito.any());
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

            Assertions.assertDoesNotThrow(() ->
                jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
            Assertions.assertEquals(manager.getUserRole(), user.getManagerUser().getUserRole());
            Mockito.verify(userService, Mockito.times( 0)).findSubordinatesByManagerUserId(Mockito.any(), Mockito.any());
            Mockito.verify(userService, Mockito.times(0)).save(Mockito.any());
        }

        @Test
        void WhenManagerIsChanged_isSubordinateFalse_thenShouldSuccess() {
            jobUpdateDto.setManagerId("3");

            User oldManager = new User();
            oldManager.setId("2");
            final UserRole managerRole = new UserRole();
            managerRole.setName(User.Role.MANAGER.name());
            oldManager.setUserRole(managerRole);

            user.setManagerUser(oldManager);

            User newManager = new User();
            newManager.setId("3");
            UserRole newManagerRole = new UserRole();
            newManagerRole.setName(User.Role.EMPLOYEE.name());
            newManager.setUserRole(newManagerRole);

            List<User> subordinatesList = new ArrayList<>();
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

            Assertions.assertDoesNotThrow(() ->
                jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
            Assertions.assertEquals(jobUpdateDto.getManagerId(), user.getManagerUser().getId());
            Mockito.verify(userService, Mockito.times(1)).findSubordinatesByManagerUserId(Mockito.any(), Mockito.any());
            Mockito.verify(userService, Mockito.times(2)).save(Mockito.any());
        }

        @Test
        void WhenManagerIsChanged_isSubordinateTrue_thenShouldSuccess() {
            jobUpdateDto.setManagerId("3");

            User oldManager = new User();
            oldManager.setId("2");
            final UserRole managerRole = new UserRole();
            managerRole.setName(User.Role.MANAGER.name());
            oldManager.setUserRole(managerRole);

            user.setManagerUser(oldManager);

            User subordinate = new User();
            subordinate.setId("4");
            subordinate.setManagerUser(user);


            User newManager = new User();
            newManager.setId("3");
            UserRole newManagerRole = new UserRole();
            newManagerRole.setName(User.Role.EMPLOYEE.name());
            newManager.setUserRole(newManagerRole);
            newManager.setManagerUser(subordinate);

            List<User> subordinatesList = new ArrayList<>();
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

            Assertions.assertDoesNotThrow(() ->
                jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
            Assertions.assertEquals(jobUpdateDto.getManagerId(), user.getManagerUser().getId());
            Assertions.assertEquals(oldManager.getId(), user.getManagerUser().getManagerUser().getId());
            Mockito.verify(userService, Mockito.times(1)).findSubordinatesByManagerUserId(Mockito.any(), Mockito.any());
            Mockito.verify(userService, Mockito.times(2)).save(Mockito.any());
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
        final BasicJobInformationDto jobInformation = jobUserService
            .findJobMessage(targetUserId, userId);
        Assertions.assertNotNull(jobInformation);
      }

      @Test
      void whenIsCurrentUser_thenReturnJobInformation() {
        final BasicJobInformationDto jobInformation = jobUserService
            .findJobMessage(targetUserId, targetUserId);
        Assertions.assertTrue(jobInformation instanceof JobInformationDto);
      }

      @Test
      void whenIsAdmin_thenReturnJobInformation() {
        final UserRole adminRole = new UserRole();
        adminRole.setName(Role.ADMIN.name());
        currentUser.setUserRole(adminRole);
        final BasicJobInformationDto jobInformation = jobUserService
            .findJobMessage(targetUserId, userId);
        Assertions.assertTrue(jobInformation instanceof JobInformationDto);
      }

      @Test
      void whenIsUserManager_thenReturnJobInformation() {
        jobUser.getUser().setManagerUser(currentUser);
        final BasicJobInformationDto jobInformation = jobUserService
            .findJobMessage(targetUserId, userId);
        Assertions.assertTrue(jobInformation instanceof JobInformationDto);
      }

      @Test
      void whenIsEmployee_thenReturnBasicJobInformation() {
        final User randomManagerUser = new User();
        jobUser.getUser().setManagerUser(currentUser);
        randomManagerUser.setId(RandomStringUtils.randomAlphabetic(16));
        final BasicJobInformationDto jobInformation = jobUserService
            .findJobMessage(targetUserId, userId);
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
        Job job = new Job();
        job.setId("1");

        Mockito.when(jobService.findById(job.getId())).thenReturn(job);
        Mockito.when(jobService.save(job)).thenReturn(job);
        Assertions.assertDoesNotThrow(() -> jobUserService.updateJobSelectOption(jobSelectOptionUpdateDto));
        Mockito.verify(jobService, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(jobService, Mockito.times(1)).findById(Mockito.any());
      }

      @Test
      void whenUpdatedFieldIsEmployeeType_thenUpdateShouldSuccess() {
        jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.EMPLOYMENT_TYPE);
        EmploymentType employmentType = new EmploymentType();
        employmentType.setId("1");

        Mockito.when(employmentTypeService.findById(employmentType.getId())).thenReturn(employmentType);
        Mockito.when(employmentTypeService.save(employmentType)).thenReturn(employmentType);
        Assertions.assertDoesNotThrow(() -> jobUserService.updateJobSelectOption(jobSelectOptionUpdateDto));
        Mockito.verify(employmentTypeService, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(employmentTypeService, Mockito.times(1)).findById(Mockito.any());
      }

      @Test
      void whenUpdatedFieldIsOfficeLocation_thenUpdateShouldSuccess() {
        jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.OFFICE_LOCATION);
        Office office = new Office();
        office.setId("1");
        office.setOfficeAddress(new OfficeAddress());

        OfficeCreateDto officeCreateDto = new OfficeCreateDto();
        officeCreateDto.setOfficeName("office name");
        officeCreateDto.setCity("city");
        officeCreateDto.setStateId("1");
        officeCreateDto.setStreet1("s1");
        officeCreateDto.setStreet2("s2");
        officeCreateDto.setZip("zip");
        jobSelectOptionUpdateDto.setOfficeCreateDto(officeCreateDto);

        final OfficeAddress officeAddress = officeAddressMapper
            .updateFromOfficeCreateDto(office.getOfficeAddress(), officeCreateDto);

        final Office newOffice = officeMapper.convertToOffice(office, officeCreateDto, officeAddress);

        Mockito.when(officeService.findById(office.getId())).thenReturn(office);
        Mockito.when(officeService.save(newOffice)).thenReturn(newOffice);
        Assertions.assertDoesNotThrow(() -> jobUserService.updateJobSelectOption(jobSelectOptionUpdateDto));
        Mockito.verify(officeService, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(officeService, Mockito.times(1)).findById(Mockito.any());
      }

      @Test
      void whenUpdatedFieldIsDepartment_thenUpdateShouldSuccess() {
        jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.DEPARTMENT);
        Department department = new Department();
        department.setId("1");

        Mockito.when(departmentService.findById(department.getId())).thenReturn(department);
        Mockito.when(departmentService.save(department)).thenReturn(department);
        Assertions.assertDoesNotThrow(() -> jobUserService.updateJobSelectOption(jobSelectOptionUpdateDto));
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
        Mockito.when(departmentService.findCountByDepartment(jobSelectOptionUpdateDto.getId())).thenReturn(10);
        Assertions.assertThrows(ForbiddenException.class, () -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
        Mockito.verify(departmentService, Mockito.times(0)).delete(Mockito.any());
        Mockito.verify(jobService, Mockito.times(0)).findAllByDepartmentId(Mockito.any());
        Mockito.verify(jobService, Mockito.times(0)).deleteInBatch(Mockito.any());

      }

      @Test
      void whenDepartmentHasNoEmployee_departmentHasNoJobs_thenDeleteDepartmentShouldSuccess() {
        jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.DEPARTMENT);
        Mockito.when(departmentService.findCountByDepartment(jobSelectOptionUpdateDto.getId())).thenReturn(0);
        Mockito.when(jobService.findAllByDepartmentId(jobSelectOptionUpdateDto.getId())).thenReturn(Collections.emptyList());
        Assertions.assertDoesNotThrow(() -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
        Mockito.verify(jobService, Mockito.times(1)).findAllByDepartmentId(Mockito.any());
        Mockito.verify(jobService, Mockito.times(0)).deleteInBatch(Mockito.any());
        Mockito.verify(departmentService, Mockito.times(1)).delete(Mockito.any());
      }

      @Test
      void whenDepartmentHasNoEmployee_departmentHasJobs_thenDeleteDepartmentShouldSuccess() {
        jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.DEPARTMENT);
        Job job = new Job();
        job.setId("1");
        List<Job> jobs = new LinkedList<>();
        jobs.add(job);

        Mockito.when(departmentService.findCountByDepartment(jobSelectOptionUpdateDto.getId())).thenReturn(0);
        Mockito.when(jobService.findAllByDepartmentId(jobSelectOptionUpdateDto.getId())).thenReturn(jobs);
        Assertions.assertDoesNotThrow(() -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
        Mockito.verify(jobService, Mockito.times(1)).findAllByDepartmentId(Mockito.any());
        Mockito.verify(jobService, Mockito.times(1)).deleteInBatch(Mockito.any());
        Mockito.verify(departmentService, Mockito.times(1)).delete(Mockito.any());
      }

      @Test
      void whenEmployeeBelongToThisJobTitle_thenDeleteJobTitleShouldFail() {
        jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.JOB_TITLE);
        Mockito.when(jobUserService.getCountByJobId(jobSelectOptionUpdateDto.getId())).thenReturn(10);
        Assertions.assertThrows(ForbiddenException.class, () -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
        Mockito.verify(jobService, Mockito.times(0)).delete(Mockito.any());
      }

      @Test
      void whenNoEmployeeBelongsToThisJobTitle_thenDeleteJobTitleShouldSuccess() {
        jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.JOB_TITLE);
        Mockito.when(departmentService.findCountByDepartment(jobSelectOptionUpdateDto.getId())).thenReturn(0);
        Assertions.assertDoesNotThrow(() -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
        Mockito.verify(jobService, Mockito.times(1)).delete(Mockito.any());
      }

      @Test
      void whenEmployeeBelongToThisEmployeeType_thenDeleteEmployeeTypeShouldFail() {
        jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.EMPLOYMENT_TYPE);
        Mockito.when(employmentTypeService.findCountByType(jobSelectOptionUpdateDto.getId())).thenReturn(10);
        Assertions.assertThrows(ForbiddenException.class, () -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
        Mockito.verify(employmentTypeService, Mockito.times(0)).delete(Mockito.any());
      }

      @Test
      void whenNoEmployeeBelongsToThisEmployeeType_thenDeleteEmployeeTypeShouldSuccess() {
        jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.EMPLOYMENT_TYPE);
        Mockito.when(departmentService.findCountByDepartment(jobSelectOptionUpdateDto.getId())).thenReturn(0);
        Assertions.assertDoesNotThrow(() -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
        Mockito.verify(employmentTypeService, Mockito.times(1)).delete(Mockito.any());
      }

      @Test
      void whenEmployeeBelongToThisOffice_thenDeleteOfficeShouldFail() {
        jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.OFFICE_LOCATION);
        Mockito.when(officeService.findCountByOffice(jobSelectOptionUpdateDto.getId())).thenReturn(10);
        Assertions.assertThrows(ForbiddenException.class, () -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
        Mockito.verify(officeService, Mockito.times(0)).delete(Mockito.any());
        Mockito.verify(officeAddressService, Mockito.times(0)).delete(Mockito.any());

      }

      @Test
      void whenNoEmployeeBelongsToThisOffice_thenDeleteOfficeShouldSuccess() {
        jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.OFFICE_LOCATION);
        Office office = new Office();
        OfficeAddress officeAddress = new OfficeAddress();
        office.setOfficeAddress(officeAddress);

        Mockito.when(officeService.findCountByOffice(jobSelectOptionUpdateDto.getId())).thenReturn(0);
        Mockito.when(officeService.findById(jobSelectOptionUpdateDto.getId())).thenReturn(office);
        Assertions.assertDoesNotThrow(() -> jobUserService.deleteJobSelectOption(jobSelectOptionUpdateDto));
        Mockito.verify(officeService, Mockito.times(1)).delete(Mockito.any());
        Mockito.verify(officeAddressService, Mockito.times(1)).delete(Mockito.any());
      }

    }

    @Test
    void findJobsByDepartmentId() {
      String id = "1";
      Job job1 = new Job();
      job1.setId("1");
      List<Job> jobs = new LinkedList<>();
      jobs.add(job1);

      Mockito.when(jobService.findAllByDepartmentId(id)).thenReturn(jobs);
      Mockito.when(jobUserService.getCountByJobId(job1.getId())).thenReturn(2);
      Assertions.assertDoesNotThrow(() -> jobUserService.findJobsByDepartmentId(id));

    }
}
