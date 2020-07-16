package shamu.company.attendance;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticEmployeesTaTimeType;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.service.OvertimeService;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.entity.UserCompensation;

public class OvertimeServiceTests {
  @InjectMocks OvertimeService overtimeService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void findAllOvertimeHours() {
    final EmployeeTimeLog employeeTimeLog = new EmployeeTimeLog();
    employeeTimeLog.setId("1");
    employeeTimeLog.setDurationMin(600);
    employeeTimeLog.setStart(Timestamp.valueOf(LocalDateTime.parse("2020-07-03T11:00:00")));
    final StaticEmployeesTaTimeType staticEmployeesTaTimeType = new StaticEmployeesTaTimeType();
    staticEmployeesTaTimeType.setName("WORK");
    employeeTimeLog.setTimeType(staticEmployeesTaTimeType);
    final List<EmployeeTimeLog> employeeTimeLogs = new ArrayList<>();
    employeeTimeLogs.add(employeeTimeLog);
    final TimeSheet timeSheet = new TimeSheet();
    final TimePeriod timePeriod = new TimePeriod();
    timePeriod.setStartDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-01T01:00:00")));
    timePeriod.setEndDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-08T01:00:00")));
    timeSheet.setTimePeriod(timePeriod);
    final UserCompensation userCompensation = new UserCompensation();
    final CompensationFrequency compensationFrequency = new CompensationFrequency();
    compensationFrequency.setName("Per Hour");
    final CompensationOvertimeStatus compensationOvertimeStatus = new CompensationOvertimeStatus();
    compensationOvertimeStatus.setName("California");
    userCompensation.setOvertimeStatus(compensationOvertimeStatus);
    userCompensation.setCompensationFrequency(compensationFrequency);
    userCompensation.setWageCents(BigInteger.valueOf(10));
    timeSheet.setUserCompensation(userCompensation);
    final StaticTimezone staticTimezone = new StaticTimezone();
    staticTimezone.setName("Asia/Shanghai");
    final CompanyTaSetting companyTaSetting = new CompanyTaSetting();
    companyTaSetting.setTimeZone(staticTimezone);

    assertThatCode(
            () ->
                overtimeService.findAllOvertimeHours(employeeTimeLogs, timeSheet, companyTaSetting))
        .doesNotThrowAnyException();
  }
}
