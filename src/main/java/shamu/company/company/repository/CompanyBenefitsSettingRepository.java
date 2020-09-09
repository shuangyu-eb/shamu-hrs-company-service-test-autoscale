package shamu.company.company.repository;

import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.CompanyBenefitsSetting;

public interface CompanyBenefitsSettingRepository
    extends BaseRepository<CompanyBenefitsSetting, String> {

  CompanyBenefitsSetting findCompanyBenefitsSettingByCompanyId(String id);
}
