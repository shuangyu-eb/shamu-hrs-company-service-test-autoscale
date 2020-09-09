package shamu.company.company.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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
    final List<CompanyBenefitsSetting> results = companyBenefitsSettingRepository.findAll();
    if (CollectionUtils.isEmpty(results)) {
      return null;
    }
    return results.get(0);
  }

  public CompanyBenefitsSetting save(final CompanyBenefitsSetting benefitsSetting) {
    return companyBenefitsSettingRepository.save(benefitsSetting);
  }
}
