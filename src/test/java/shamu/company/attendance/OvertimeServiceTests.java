package shamu.company.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.dto.NewOvertimePolicyDetailDto;
import shamu.company.attendance.dto.NewOvertimePolicyDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.OvertimePolicy;
import shamu.company.attendance.entity.PolicyDetail;
import shamu.company.attendance.entity.StaticEmployeesTaTimeType;
import shamu.company.attendance.entity.StaticOvertimeType;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.entity.mapper.OvertimePolicyMapper;
import shamu.company.attendance.entity.mapper.PolicyDetailMapper;
import shamu.company.attendance.repository.OvertimePolicyRepository;
import shamu.company.attendance.repository.PolicyDetailRepository;
import shamu.company.attendance.repository.StaticOvertimeTypeRepository;
import shamu.company.attendance.service.OvertimeService;
import shamu.company.company.entity.Company;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.entity.UserCompensation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

public class OvertimeServiceTests {
  @InjectMocks OvertimeService overtimeService;

  @Mock StaticOvertimeTypeRepository staticOvertimeTypeRepository;

  @Mock OvertimePolicyMapper overtimePolicyMapper;

  @Mock PolicyDetailMapper policyDetailMapper;

  @Mock OvertimePolicyRepository overtimePolicyRepository;

  @Mock PolicyDetailRepository policyDetailRepository;

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

  @Test
  void saveNewOvertimePolicy() {
    final StaticOvertimeType staticOvertimeType = new StaticOvertimeType();
    staticOvertimeType.setName("DAILY");
    final NewOvertimePolicyDto overtimePolicyDto = new NewOvertimePolicyDto();
    final List<NewOvertimePolicyDetailDto> policyDetailDtos = new ArrayList<>();
    final NewOvertimePolicyDetailDto overtimePolicyDetailDto = new NewOvertimePolicyDetailDto();
    final PolicyDetail policyDetail = new PolicyDetail();
    policyDetail.setStaticOvertimeType(staticOvertimeType);
    overtimePolicyDetailDto.setStartMin(480);
    overtimePolicyDetailDto.setOvertimeRate(1.5);
    overtimePolicyDetailDto.setOvertimeType(StaticOvertimeType.OvertimeType.DAILY);
    policyDetailDtos.add(overtimePolicyDetailDto);
    overtimePolicyDto.setPolicyDetails(policyDetailDtos);
    Mockito.when(staticOvertimeTypeRepository.findByName(Mockito.any()))
        .thenReturn(staticOvertimeType);
    Mockito.when(policyDetailMapper.convertToPolicyDetail(Mockito.any(), Mockito.any()))
        .thenReturn(policyDetail);
    assertThatCode(() -> overtimeService.saveNewOvertimePolicy(overtimePolicyDto, "1"))
        .doesNotThrowAnyException();
  }

  @Test
  void getOvertimeRules() {
    final OvertimePolicy overtimePolicy = new OvertimePolicy();
    overtimePolicy.setId("1");
    final UserCompensation userCompensation = new UserCompensation();
    userCompensation.setOvertimePolicy(overtimePolicy);
    final TimeSheet timeSheet = new TimeSheet();
    timeSheet.setUserCompensation(userCompensation);
    final List<PolicyDetail> policyDetails = new ArrayList<>();
    final PolicyDetail policyDetail = new PolicyDetail();
    policyDetail.setStart(8);
    policyDetail.setRate(BigDecimal.valueOf(1.5));
    final StaticOvertimeType staticOvertimeType = new StaticOvertimeType();
    staticOvertimeType.setName("DAILY");
    policyDetail.setStaticOvertimeType(staticOvertimeType);
    policyDetails.add(policyDetail);
    Mockito.when(policyDetailRepository.findAllByOvertimePolicyId(overtimePolicy.getId()))
        .thenReturn(policyDetails);
    assertThatCode(() -> overtimeService.getOvertimeRules(timeSheet)).doesNotThrowAnyException();
  }

  @Nested
  class policy {
    Company company = new Company();

    @Test
    void whenCompanyValid_createDefaultPolicy_shouldSucceed() {
      Mockito.when(overtimePolicyRepository.save(Mockito.any())).thenReturn(new OvertimePolicy());
      assertThatCode(() -> overtimeService.createDefaultPolicy(company)).doesNotThrowAnyException();
    }

    @Test
    void findDefaultPolicy_shouldSucceed() {
      final String companyId = "test_company_id";
      Mockito.when(overtimePolicyRepository.findByCompanyIdAndDefaultPolicyIsTrue(companyId))
          .thenReturn(new OvertimePolicy());
      assertThatCode(() -> overtimeService.findDefaultPolicy(companyId)).doesNotThrowAnyException();
    }

    @Test
    void softDeletePolicy_shouldSucceed() {
      final String overtimeId = "test_overtime_id";

      assertThatCode(() -> overtimeService.softDeleteOvertimePolicy(overtimeId))
          .doesNotThrowAnyException();
    }
  }

  @Test
  void findOvertimePolicyDetails() {
    final OvertimePolicy overtimePolicy = new OvertimePolicy();
    final PolicyDetail policyDetail = new PolicyDetail();
    final List<PolicyDetail> policyDetails = new ArrayList<>();
    policyDetails.add(policyDetail);
    Mockito.when(overtimePolicyRepository.findById("1"))
        .thenReturn(java.util.Optional.of(overtimePolicy));
    Mockito.when(policyDetailRepository.findAllByOvertimePolicyId("1")).thenReturn(policyDetails);
    assertThatCode(() -> overtimeService.findOvertimePolicyDetails("1")).doesNotThrowAnyException();
  }
}
