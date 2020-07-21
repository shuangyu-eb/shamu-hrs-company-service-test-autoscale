package shamu.company.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.repository.PayPeriodFrequencyRepository;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.PayPeriodFrequencyService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;

public class PayPeriodFrequencyServiceTest {

  @InjectMocks PayPeriodFrequencyService payPeriodFrequencyService;

  @Mock private PayPeriodFrequencyRepository payPeriodFrequencyRepository;

  @Mock private AttendanceSettingsService attendanceSettingsService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class find {
    String name;
    String id;
    String companyId;

    @BeforeEach
    void init() {
      name = "test_name";
      id = "test_id";
      companyId = "test_company_id";
    }

    @Test
    void findAllShouldSucceed() {
      assertThatCode(
              () -> {
                payPeriodFrequencyService.findAll();
              })
          .doesNotThrowAnyException();
    }

    @Test
    void findByNameShouldSucceed() {
      Mockito.when(payPeriodFrequencyRepository.findByName(name))
          .thenReturn(new StaticCompanyPayFrequencyType());
      assertThatCode(
              () -> {
                payPeriodFrequencyService.findByName(name);
              })
          .doesNotThrowAnyException();
    }

    @Test
    void findByIdShouldSucceed() {
      Mockito.when(payPeriodFrequencyRepository.findById(id))
          .thenReturn(Optional.of(new StaticCompanyPayFrequencyType()));
      assertThatCode(
              () -> {
                payPeriodFrequencyService.findByName(id);
              })
          .doesNotThrowAnyException();
    }

    @Test
    void findByCompanyShouldSucceed() {
      final CompanyTaSetting companyTaSetting = new CompanyTaSetting();
      companyTaSetting.setPayFrequencyType(new StaticCompanyPayFrequencyType());
      Mockito.when(attendanceSettingsService.findCompanySettings(companyId))
          .thenReturn(companyTaSetting);
      assertThatCode(
              () -> {
                payPeriodFrequencyService.findByCompany(companyId);
              })
          .doesNotThrowAnyException();
    }
  }
}
