package shamu.company.attendance.repository;

import org.springframework.data.jpa.repository.Query;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.common.repository.BaseRepository;

public interface CompanyTaSettingRepository extends BaseRepository<CompanyTaSetting, String> {
  Boolean existsByCompanyId(final String companyId);

  CompanyTaSetting findByCompanyId(final String companyId);

  @Query(
      value =
          "select approval_days_before_payroll from company_ta_settings where company_id = unhex(?1)",
      nativeQuery = true)
  int findApprovalDaysBeforePayroll(final String companyId);
}
