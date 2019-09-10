package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.PaidHolidayUser;

public interface PaidHolidayUserRepository extends BaseRepository<PaidHolidayUser, Long> {

  @Query(
      value = "SELECT * FROM paid_holidays_users WHERE company_id = ?1 AND deleted_at IS NULL",
      nativeQuery = true
  )
  List<PaidHolidayUser> findAllByCompanyId(Long id);

  @Query(
      value = "SELECT user_id"
          + " FROM paid_holidays_users"
          + " WHERE company_id = ?1 AND deleted_at IS NULL",
      nativeQuery = true
  )
  List<Long> findAllUserIdByCompanyId(Long companyId);

  @Query(
      value = "SELECT * FROM paid_holidays_users WHERE company_id = ?1 AND user_id = ?2 "
          + "AND deleted_at IS NULL",
      nativeQuery = true
  )
  PaidHolidayUser findByCompanyIdAndUserId(Long companyId,Long userId);

}
