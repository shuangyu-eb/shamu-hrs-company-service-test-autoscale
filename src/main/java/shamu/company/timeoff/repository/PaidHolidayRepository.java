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
public interface PaidHolidayRepository extends BaseRepository<PaidHoliday, String> {

  @Query(
      value = "SELECT * FROM paid_holidays WHERE company_id = unhex(?1)",
      nativeQuery = true
  )
  List<PaidHoliday> findByCompanyId(String companyId);

  @Query(
      value = "SELECT * FROM paid_holidays WHERE company_id IS NULL",
      nativeQuery = true
  )
  List<PaidHoliday> findDefaultPaidHolidays();

  @Modifying
  @Transactional
  @Query(
      value = "UPDATE paid_holidays SET name = ?2, name_show = ?2,"
          + " date = ?3 WHERE id = unhex(?1)",
      nativeQuery = true
  )
  void updateDetail(String id, String name, Timestamp date);

  @Modifying
  @Transactional
  @Query(
      value = "UPDATE companies_paid_holidays "
          + "SET is_selected = ?2 "
          + "WHERE paid_holiday_id = unhex(?1)",
      nativeQuery = true
  )
  void updateHolidaySelect(String id, Boolean isSelected);
}
