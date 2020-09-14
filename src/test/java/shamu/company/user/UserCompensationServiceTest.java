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
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.service.CompensationFrequencyService;
import shamu.company.user.service.UserCompensationService;

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

    @Test
    void whenPoliciesValid_thenShouldSucceed() {
      final EmployeeOvertimeDetailsDto employeeOvertimeDetailsDto =
          new EmployeeOvertimeDetailsDto();
      employeeOvertimeDetailsDto.setEmployeeId(userId);
      employeeOvertimeDetailsDto.setRegularPay(regularPay);
      employeeOvertimeDetailsDto.setCompensationUnit(compensationUnit);
      employeeOvertimeDetailsDto.setOvertimePolicy(policyName);
      final OvertimePolicy overtimePolicy = new OvertimePolicy();
      overtimePolicy.setPolicyName(policyName);
      Mockito.when(userCompensationMapper.updateCompensationCents(regularPay))
          .thenReturn(Mockito.any());
      Mockito.when(compensationFrequencyService.findById(compensationUnit))
          .thenReturn(new CompensationFrequency());
      assertThatCode(
          () ->
              userCompensationService.saveAllByEmployeeOvertimePolicies(
                  Arrays.asList(employeeOvertimeDetailsDto),
                  Arrays.asList(overtimePolicy),
                  new Date()));
    }
  }
}
