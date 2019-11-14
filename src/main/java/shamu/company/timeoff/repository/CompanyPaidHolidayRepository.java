package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.CompanyPaidHoliday;

public interface CompanyPaidHolidayRepository extends BaseRepository<CompanyPaidHoliday, String> {

  @Query(value = "SELECT * "
      + "FROM companies_paid_holidays "
      + "WHERE company_id = unhex(?1) ",
      nativeQuery = true)
  List<CompanyPaidHoliday> findAllByCompanyId(String companyId);

  @Query(value = "select cph.* "
          + "from companies_paid_holidays cph "
          + "where cph.company_id = ( "
          + "   select phu.company_id "
          + "   from paid_holidays_users phu "
          + "   where phu.company_id = unhex(?1) "
          + "   and phu.user_id = unhex(?2) "
          + "   and phu.is_selected = true ) ",
          nativeQuery = true)
  List<CompanyPaidHoliday> findAllByCompanyIdAndUserId(String companyId, String userId);

  CompanyPaidHoliday findCompanyPaidHolidayByPaidHolidayIdAndCompanyId(String paidHolidayId,
      String companyId);

  @Modifying
  @Transactional
  @Query(
          value = "delete from "
                  + "companies_paid_holidays cph "
                  + "where "
                  + "cph.paid_holiday_id=unhex(?1) ",
          nativeQuery = true)
  void deleteByPaidHolidayId(String id);
}
