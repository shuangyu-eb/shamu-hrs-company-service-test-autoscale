package shamu.company.attendance.repository;

import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.common.repository.BaseRepository;

public interface CompanyTaSettingRepository extends BaseRepository<CompanyTaSetting, String> {
  Boolean existsByCompanyId(final String companyId);

  CompanyTaSetting findByCompanyId(final String companyId);
}
