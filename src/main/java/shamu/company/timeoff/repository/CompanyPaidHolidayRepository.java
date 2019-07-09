package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.CompanyPaidHoliday;

public interface CompanyPaidHolidayRepository extends BaseRepository<CompanyPaidHoliday, Long> {

  @Query(value = "SELECT * "
      + "FROM companies_paid_holidays "
      + "WHERE company_id = ?1 "
      + "AND deleted_at IS NULL",
      nativeQuery = true)
  List<CompanyPaidHoliday> findAllByCompanyId(Long companyId);
}
