package shamu.company.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.repository.CompensationOvertimeStatusRepository;
import shamu.company.user.service.CompensationOvertimePolicyService;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatCode;

public class CompensationOvertimePolicyServiceTests {
  @Mock CompensationOvertimeStatusRepository compensationOvertimeStatusRepository;
  @InjectMocks CompensationOvertimePolicyService compensationOvertimePolicyService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class GetCompensationOvertimeStatus {
    String companyId = "test_company_id";
    String name = "test_name";

    @Test
    void whenCompanyIdValid_shouldSucceed() {
      Mockito.when(compensationOvertimeStatusRepository.findByCompanyId(companyId))
          .thenReturn(new ArrayList<>());
      assertThatCode(() -> compensationOvertimePolicyService.findAll(companyId))
          .doesNotThrowAnyException();
    }

    @Test
    void whenCompanyIdAndNameValid_shouldSucceed() {
      Mockito.when(compensationOvertimeStatusRepository.findByCompanyIdAndName(companyId, name))
          .thenReturn(new CompensationOvertimeStatus());
      assertThatCode(() -> compensationOvertimePolicyService.findByName(companyId, name))
          .doesNotThrowAnyException();
    }
  }
}
