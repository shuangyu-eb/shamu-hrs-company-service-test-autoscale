package shamu.company.attendance.repository;

import org.springframework.data.jpa.repository.Query;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.common.repository.BaseRepository;

public interface CompanyTaSettingRepository extends BaseRepository<CompanyTaSetting, String> {

  @Query(
      value =
          "select approval_days_before_payroll from company_ta_settings limit 1",
      nativeQuery = true)
  int findApprovalDaysBeforePayroll();
}
