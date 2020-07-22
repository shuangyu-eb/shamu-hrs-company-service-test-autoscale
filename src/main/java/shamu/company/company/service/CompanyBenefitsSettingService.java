package shamu.company.company.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.company.entity.CompanyBenefitsSetting;
import shamu.company.company.repository.CompanyBenefitsSettingRepository;

@Service
@Transactional
public class CompanyBenefitsSettingService {

  private final CompanyBenefitsSettingRepository companyBenefitsSettingRepository;

  public CompanyBenefitsSettingService(
      final CompanyBenefitsSettingRepository companyBenefitsSettingRepository) {
    this.companyBenefitsSettingRepository = companyBenefitsSettingRepository;
  }

  public CompanyBenefitsSetting getCompanyBenefitsSetting() {
    return companyBenefitsSettingRepository.findAll().get(0);
  }

  public CompanyBenefitsSetting save(final CompanyBenefitsSetting benefitsSetting) {
    return companyBenefitsSettingRepository.save(benefitsSetting);
  }
}
