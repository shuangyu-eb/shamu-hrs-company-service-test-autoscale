package shamu.company.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.dto.EmployeeOvertimeDetailsDto;
import shamu.company.attendance.entity.OvertimePolicy;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.repository.OvertimePolicyRepository;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.JobUser;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.service.CompensationFrequencyService;
import shamu.company.user.service.UserCompensationService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class UserCompensationServiceTest {
  @Mock private UserCompensationRepository userCompensationRepository;

  @Mock private UserCompensationMapper userCompensationMapper;

  @Mock private CompensationFrequencyService compensationFrequencyService;

  @InjectMocks private UserCompensationService userCompensationService;

  @Mock private OvertimePolicyRepository overtimePolicyRepository;

  @Mock private TimeSheetService timeSheetService;

  @Mock private JobUserRepository jobUserRepository;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenSave_thenShouldCall() {
    final UserCompensation userCompensation = new UserCompensation();
    userCompensationService.save(userCompensation);
    Mockito.verify(userCompensationRepository, Mockito.times(1)).save(Mockito.any());
  }

  @Nested
  class findTimeSheetById {
    UserCompensation userCompensation;

    @BeforeEach
    void init() {
      userCompensation = new UserCompensation();
    }

    @Test
    void whenIdExists_thenShouldSuccess() {
      Mockito.when(userCompensationRepository.findById(Mockito.anyString()))
          .thenReturn(Optional.ofNullable(userCompensation));
      assertThatCode(() -> userCompensationService.findCompensationById("1"))
          .doesNotThrowAnyException();
    }

    @Test
    void whenIdNotExists_thenShouldThrow() {
      Mockito.when(userCompensationRepository.findById(Mockito.anyString()))
          .thenReturn(Optional.empty());
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> userCompensationService.findCompensationById("1"));
    }
  }

  @Nested
  class save {
    UserCompensation userCompensation;

    @BeforeEach
    void init() {
      userCompensation = new UserCompensation();
    }

    @Test
    void whenCompensationValid_thenShouldSucceed() {
      final List<UserCompensation> userCompensationList = new ArrayList<>();
      userCompensationList.add(userCompensation);
      Mockito.when(userCompensationRepository.saveAll(userCompensationList))
          .thenReturn(userCompensationList);
      assertThatCode(() -> userCompensationService.saveAll(userCompensationList))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  class SaveEmployeeOvertimePolicy {
    String userId = "test_user_id";
    Double regularPay = 1.0;
    String compensationUnit = "compensation_unit";
    String policyName = "policy_name";
    final EmployeeOvertimeDetailsDto employeeOvertimeDetailsDto = new EmployeeOvertimeDetailsDto();
    final UserCompensation userCompensation = new UserCompensation();

    @BeforeEach
    void init() {
      employeeOvertimeDetailsDto.setEmployeeId(userId);
      employeeOvertimeDetailsDto.setRegularPay(regularPay);
      employeeOvertimeDetailsDto.setCompensationUnit(compensationUnit);
      employeeOvertimeDetailsDto.setOvertimePolicy(policyName);
      userCompensation.setEndDate(new Timestamp(new Date().getTime()));
    }

    @Test
    void whenPoliciesValid_thenShouldSucceed() {
      final OvertimePolicy overtimePolicy = new OvertimePolicy();
      overtimePolicy.setPolicyName(policyName);
      Mockito.when(userCompensationMapper.updateCompensationCents(regularPay))
          .thenReturn(Mockito.any());
      Mockito.when(compensationFrequencyService.findById(compensationUnit))
          .thenReturn(new CompensationFrequency());
      Mockito.when(userCompensationRepository.findByUserId(userId)).thenReturn(userCompensation);
      Mockito.when(overtimePolicyRepository.findAll()).thenReturn(Arrays.asList(overtimePolicy));
      assertThatCode(
              () ->
                  userCompensationService.updateByCreateEmployeeOvertimePolicies(
                      Arrays.asList(employeeOvertimeDetailsDto), new Date()))
          .doesNotThrowAnyException();
    }

    @Test
    void updateByEditEmployeeOvertimePolicies_shouldSucceed() {
      Mockito.when(timeSheetService.findCurrentByUseCompensation(Mockito.any()))
          .thenReturn(new TimeSheet());
      Mockito.when(userCompensationRepository.findByUserId(userId)).thenReturn(userCompensation);
      Mockito.when(jobUserRepository.findByUserId(Mockito.any())).thenReturn(new JobUser());
      assertThatCode(
              () ->
                  userCompensationService.updateByEditEmployeeOvertimePolicies(
                      Arrays.asList(employeeOvertimeDetailsDto)))
          .doesNotThrowAnyException();
    }

    @Test
    void removeEmployees() {
      Mockito.when(userCompensationRepository.findByUserIdIn(Mockito.any()))
          .thenReturn(Arrays.asList(userCompensation));
      Mockito.when(jobUserRepository.findByUserId(Mockito.any())).thenReturn(new JobUser());
      assertThatCode(
              () -> userCompensationService.removeUsersFromAttendance((Arrays.asList(userId))))
          .doesNotThrowAnyException();
    }
  }

  @Test
  void updateByEditOvertimePolicyDetails() {
    final UserCompensation userCompensation = new UserCompensation();
    final List<UserCompensation> userCompensations = new ArrayList<>();
    final OvertimePolicy overtimePolicy = new OvertimePolicy();
    final JobUser jobUser = new JobUser();
    userCompensations.add(userCompensation);
    Mockito.when(userCompensationRepository.findCurrentByOvertimePolicyId("1"))
        .thenReturn(userCompensations);
    Mockito.when(jobUserRepository.findByUserId(Mockito.any())).thenReturn(jobUser);
    assertThatCode(
            () -> userCompensationService.updateByEditOvertimePolicyDetails("1", overtimePolicy))
        .doesNotThrowAnyException();
  }
}
