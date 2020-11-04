package shamu.company.payroll;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import shamu.company.attendance.dto.AttendanceDetailDto;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.employee.dto.CompensationDto;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.financialengine.dto.CompanyTaxIdDto;
import shamu.company.financialengine.service.FECompanyService;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.job.service.JobUserService;
import shamu.company.payroll.service.PayrollSetUpService;
import shamu.company.user.entity.EmployeeType;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.service.UserCompensationService;
import shamu.company.user.service.UserService;
import shamu.company.utils.UuidUtil;

public class PayrollSetupServiceTests {
  @InjectMocks private PayrollSetUpService payrollSetupService;

  @Mock private AttendanceSetUpService attendanceSetUpService;

  @Mock private AttendanceTeamHoursService attendanceTeamHoursService;

  @Mock private UserService userService;

  @Mock private JobUserService jobUserService;

  @Mock private UserCompensationMapper userCompensationMapper;

  @Mock private UserCompensationService userCompensationService;

  @Mock private FECompanyService feCompanyService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class getPayrollDetails {
    AttendanceDetailDto attendanceDetailDto = new AttendanceDetailDto();

    @Test
    void whenIsSetUp_thenReturnWithDetails() {
      Mockito.when(attendanceSetUpService.findIsAttendanceSetUp()).thenReturn(true);
      Mockito.when(attendanceTeamHoursService.findAttendanceDetails())
          .thenReturn(attendanceDetailDto);

      assertThatCode(() -> payrollSetupService.getPayrollDetails()).doesNotThrowAnyException();
    }
  }

  @Nested
  class getPayrollAuthorizedEmployees {
    JobUserListItem jobUserListItem = new JobUserListItem();
    String userId = "test_user_id";
    String managerId = "test_manager_id";
    EmployeeListSearchCondition employeeListSearchCondition = new EmployeeListSearchCondition();

    @BeforeEach
    void init() {
      jobUserListItem.setId(userId);
    }

    @Test
    void whenConditionValid_shouldReturnEmployees() {
      final User manager = new User();
      final UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
      userPersonalInformation.setPreferredName("test_name");
      userPersonalInformation.setLastName("test_last_name");
      manager.setUserPersonalInformation(userPersonalInformation);
      Mockito.when(userService.findAllEmployees(employeeListSearchCondition))
          .thenReturn(new PageImpl<>(Arrays.asList(jobUserListItem)));
      Mockito.when(userService.getManagerUserIdById(userId)).thenReturn(managerId);
      Mockito.when(userService.findById(managerId)).thenReturn(manager);

      assertThatCode(
              () -> payrollSetupService.getPayrollAuthorizedEmployees(employeeListSearchCondition))
          .doesNotThrowAnyException();
    }
  }

  @Test
  void testGetEmployeesCompensation() {
    final List<User> users = new ArrayList<>();
    final User user = new User(UuidUtil.getUuidString());
    final UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
    userPersonalInformation.setFirstName("test");
    userPersonalInformation.setLastName("test");
    user.setUserPersonalInformation(userPersonalInformation);
    users.add(user);

    final EmployeeType employeeType = new EmployeeType();
    employeeType.setName("test");
    final JobUser jobUser = new JobUser();
    jobUser.setEmployeeType(employeeType);
    jobUser.setStartDate(new Timestamp(System.currentTimeMillis()));

    final UserCompensation userCompensation = new UserCompensation();

    final CompensationDto compensationDto = new CompensationDto();

    Mockito.when(userService.findAllUsersByCompany()).thenReturn(users);
    Mockito.when(jobUserService.findJobUserByUser(user)).thenReturn(jobUser);
    Mockito.when(userCompensationService.findByUserId(user.getId())).thenReturn(userCompensation);
    Mockito.when(userCompensationMapper.convertToCompensationDto(userCompensation))
        .thenReturn(compensationDto);

    assertEquals(
        payrollSetupService.getPayrollSetUpEmployees().get(0).getEmployeeId(), user.getId());
  }

  @Test
  void testGetTaxList() {
    final List<CompanyTaxIdDto> companyTaxIdDtos = new ArrayList<>();
    Mockito.when(feCompanyService.getAvailableTaxList()).thenReturn(companyTaxIdDtos);

    assertThatCode(() -> payrollSetupService.getTaxList()).doesNotThrowAnyException();
  }
}
