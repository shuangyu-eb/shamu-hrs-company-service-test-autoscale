package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.CompanyPaidHoliday;

public interface CompanyPaidHolidayRepository extends BaseRepository<CompanyPaidHoliday, Long> {

  @Query(value = "SELECT * "
      + "FROM companies_paid_holidays "
      + "WHERE company_id = ?1 "
      + "AND deleted_at IS NULL",
      nativeQuery = true)
  List<CompanyPaidHoliday> findAllByCompanyId(Long companyId);

  @Modifying
  @Transactional
  @Query(
          value = "update "
                  + "companies_paid_holidays cph "
                  + "set "
                  + "cph.deleted_at=current_timestamp "
                  + "where "
                  + "cph.paid_holiday_id=?1 "
                  + "and ("
                  + "deleted_at is null"
                  + ")",
          nativeQuery = true)
  void deleteByPaidHolidayId(Long id);
}
