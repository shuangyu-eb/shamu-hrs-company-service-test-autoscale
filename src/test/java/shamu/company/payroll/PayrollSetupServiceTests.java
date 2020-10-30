package shamu.company.payroll;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.dto.AttendanceDetailDto;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.employee.dto.CompensationDto;
import shamu.company.job.entity.JobUser;
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

  @Test
  void testGetEmployeesCompensation() {
    List<User> users = new ArrayList<>();
    User user = new User(UuidUtil.getUuidString());
    UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
    userPersonalInformation.setFirstName("test");
    userPersonalInformation.setLastName("test");
    user.setUserPersonalInformation(userPersonalInformation);
    users.add(user);

    EmployeeType employeeType = new EmployeeType();
    employeeType.setName("test");
    JobUser jobUser = new JobUser();
    jobUser.setEmployeeType(employeeType);
    jobUser.setStartDate(new Timestamp(System.currentTimeMillis()));

    UserCompensation userCompensation = new UserCompensation();

    CompensationDto compensationDto = new CompensationDto();

    Mockito.when(userService.findAllUsersByCompany()).thenReturn(users);
    Mockito.when(jobUserService.findJobUserByUser(user)).thenReturn(jobUser);
    Mockito.when(userCompensationService.findByUserId(user.getId())).thenReturn(userCompensation);
    Mockito.when(userCompensationMapper.convertToCompensationDto(userCompensation))
        .thenReturn(compensationDto);

    assertEquals(
        payrollSetupService.getPayrollSetUpEmployees().get(0).getEmployeeId(), user.getId());
  }
}
