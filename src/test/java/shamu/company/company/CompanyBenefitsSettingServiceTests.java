package shamu.company.company;

import org.junit.jupiter.api.Assertions;
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
    companyBenefitsSettingService = new CompanyBenefitsSettingService(companyBenefitsSettingRepository);
  }

  @Test
  void testFindByCompanyId() {
    Assertions.assertDoesNotThrow(() -> companyBenefitsSettingService.findByCompanyId("1"));
  }

  @Test
  void testSave() {
    final CompanyBenefitsSetting companyBenefitsSetting = new CompanyBenefitsSetting();
    Assertions.assertDoesNotThrow(() -> companyBenefitsSettingService.save(companyBenefitsSetting));
  }
}
