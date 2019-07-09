package shamu.company.timeoff.repository;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.PaidHoliday;

@Repository
public interface PaidHolidayRepository extends BaseRepository<PaidHoliday, Long> {

  @Query(
      value = "SELECT * FROM paid_holidays WHERE company_id = ?1 AND deleted_at IS NULL",
      nativeQuery = true
  )
  List<PaidHoliday> findByCompanyId(Long companyId);

  @Query(
      value = "SELECT * FROM paid_holidays WHERE company_id IS NULL AND deleted_at IS NULL",
      nativeQuery = true
  )
  List<PaidHoliday> findDefaultPaidHolidays();

  @Modifying
  @Transactional
  @Query(
      value = "UPDATE paid_holidays SET name = ?2,"
          + " date = ?3 WHERE id = ?1 AND deleted_at IS NULL",
      nativeQuery = true
  )
  void updateDetail(Long id, String name, String date);

  @Modifying
  @Transactional
  @Query(
      value = "UPDATE companies_paid_holidays "
          + "SET is_selected = ?2 "
          + "WHERE paid_holiday_id = ?1 AND deleted_at IS NULL",
      nativeQuery = true
  )
  void updateHolidaySelect(Long id, Boolean isSelected);
}
