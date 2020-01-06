package shamu.company.job.service;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.service.DepartmentService;
import shamu.company.common.service.OfficeAddressService;
import shamu.company.common.service.OfficeService;
import shamu.company.company.entity.mapper.OfficeAddressMapper;
import shamu.company.company.entity.mapper.OfficeMapper;
import shamu.company.company.entity.mapper.OfficeMapperImpl;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.JobInformationDto;
import shamu.company.employee.service.EmploymentTypeService;
import shamu.company.job.dto.JobUpdateDto;
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

    @Nested
    class updateJobInfo {

        JobUpdateDto jobUpdateDto = new JobUpdateDto();

        User manager = new User();

        User user = new User();

        List<User> directReports = new ArrayList<>();

        JobUser jobUser = new JobUser();

        @BeforeEach
        void init() {
            jobUpdateDto.setJobId("1");
            jobUpdateDto.setCompensationFrequencyId("1");
            jobUpdateDto.setCompensationWage(1000);
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

            directReports.add(user);
            directReports.add(manager);
        }

        @Test
        void WhenManagerIsNotChanged_thenShouldSuccess() {

            Mockito.when(userService.findById(user.getId())).thenReturn(user);
            Mockito.when(jobUserRepository.findJobUserByUser(jobUser.getUser())).thenReturn(jobUser);
            Mockito.when(userService.findById(manager.getId())).thenReturn(manager);
            Mockito.when(userService.findDirectReportsByManagerUserId(Mockito.any(), Mockito.any()))
                    .thenReturn(directReports);
            Mockito.when(jobUserRepository.save(jobUser)).thenReturn(jobUser);
            Mockito.when(userService.save(manager)).thenReturn(manager);
            Mockito.when(userService.save(user)).thenReturn(user);

            Assertions.assertDoesNotThrow(() ->
                jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
            Assertions.assertEquals(manager.getUserRole(), user.getManagerUser().getUserRole());
            Mockito.verify(userService, Mockito.times(1)).findDirectReportsByManagerUserId(Mockito.any(), Mockito.any());
            Mockito.verify(userService, Mockito.times(1)).save(Mockito.any());
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

            List<User> directReportsEmployeesList = new ArrayList<>();
            directReportsEmployeesList.add(user);
            directReportsEmployeesList.add(oldManager);

            Mockito.when(userService.findById(user.getId())).thenReturn(user);
            Mockito.when(jobUserRepository.findJobUserByUser(jobUser.getUser())).thenReturn(jobUser);
            Mockito.when(userService.findById(oldManager.getId())).thenReturn(oldManager);
            Mockito.when(userService.findById(newManager.getId())).thenReturn(newManager);
            Mockito.when(userService.findDirectReportsByManagerUserId(Mockito.any(), Mockito.any()))
                    .thenReturn(directReportsEmployeesList);
            Mockito.when(jobUserRepository.save(jobUser)).thenReturn(jobUser);
            Mockito.when(userService.save(oldManager)).thenReturn(oldManager);
            Mockito.when(userService.save(user)).thenReturn(user);

            Assertions.assertDoesNotThrow(() ->
                jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
            Assertions.assertEquals(jobUpdateDto.getManagerId(), user.getManagerUser().getId());
            Mockito.verify(userService, Mockito.times(1)).findDirectReportsByManagerUserId(Mockito.any(), Mockito.any());
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

            User newManager = new User();
            newManager.setId("3");
            UserRole newManagerRole = new UserRole();
            newManagerRole.setName(User.Role.EMPLOYEE.name());
            newManager.setUserRole(newManagerRole);
            newManager.setManagerUser(user);

            List<User> directReportsEmployeesList = new ArrayList<>();
            directReportsEmployeesList.add(user);
            directReportsEmployeesList.add(oldManager);

            Mockito.when(userService.findById(user.getId())).thenReturn(user);
            Mockito.when(jobUserRepository.findJobUserByUser(jobUser.getUser())).thenReturn(jobUser);
            Mockito.when(userService.findById(oldManager.getId())).thenReturn(oldManager);
            Mockito.when(userService.findById(newManager.getId())).thenReturn(newManager);
            Mockito.when(userService.findDirectReportsByManagerUserId(Mockito.any(), Mockito.any()))
                    .thenReturn(directReportsEmployeesList);
            Mockito.when(jobUserRepository.save(jobUser)).thenReturn(jobUser);
            Mockito.when(userService.save(oldManager)).thenReturn(oldManager);
            Mockito.when(userService.save(user)).thenReturn(user);

            Assertions.assertDoesNotThrow(() ->
                jobUserService.updateJobInfo("1", jobUpdateDto, "1"));
            Assertions.assertEquals(jobUpdateDto.getManagerId(), user.getManagerUser().getId());
            Assertions.assertEquals(oldManager.getId(), user.getManagerUser().getManagerUser().getId());
            Mockito.verify(userService, Mockito.times(1)).findDirectReportsByManagerUserId(Mockito.any(), Mockito.any());
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
}
