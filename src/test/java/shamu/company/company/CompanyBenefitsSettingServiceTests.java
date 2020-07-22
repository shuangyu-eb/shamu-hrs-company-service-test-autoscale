package shamu.company.company;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import shamu.company.company.entity.CompanyBenefitsSetting;
import shamu.company.company.repository.CompanyBenefitsSettingRepository;
import shamu.company.company.service.CompanyBenefitsSettingService;

public class CompanyBenefitsSettingServiceTests {

  @Mock private CompanyBenefitsSettingRepository companyBenefitsSettingRepository;
  private CompanyBenefitsSettingService companyBenefitsSettingService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    companyBenefitsSettingService =
        new CompanyBenefitsSettingService(companyBenefitsSettingRepository);
  }

  @Test
  void testFindByCompanyId() {
    assertThatCode(() -> companyBenefitsSettingService.getCompanyBenefitsSetting())
        .doesNotThrowAnyException();
  }

  @Test
  void testSave() {
    final CompanyBenefitsSetting companyBenefitsSetting = new CompanyBenefitsSetting();
    assertThatCode(() -> companyBenefitsSettingService.save(companyBenefitsSetting))
        .doesNotThrowAnyException();
  }
}
