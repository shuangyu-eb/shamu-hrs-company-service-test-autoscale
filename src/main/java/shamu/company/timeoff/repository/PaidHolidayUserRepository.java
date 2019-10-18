package shamu.company.timeoff.repository;

import java.math.BigInteger;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.PaidHolidayUser;

public interface PaidHolidayUserRepository extends BaseRepository<PaidHolidayUser, Long> {
  String ACTIVE_USER_QUERY = "and (u.deactivated_at is null "
      + "or (u.deactivated_at is not null "
      + "and u.deactivated_at > current_timestamp)) ";

  @Query(
      value = "SELECT phu.* "
          + "FROM paid_holidays_users phu "
          + "LEFT JOIN users u "
          + "ON phu.user_id = u.id "
          + "WHERE phu.company_id = ?1 "
          + "AND phu.deleted_at IS NULL "
          + ACTIVE_USER_QUERY
          + "AND u.deleted_at IS NULL ",
      nativeQuery = true
  )
  List<PaidHolidayUser> findAllByCompanyId(Long id);

  @Query(
      value = "SELECT phu.user_id "
          + "FROM paid_holidays_users phu "
          + "LEFT JOIN users u "
          + "ON phu.user_id = u.id "
          + "WHERE phu.company_id = ?1 "
          + "AND phu.deleted_at IS NULL "
          + ACTIVE_USER_QUERY
          + "AND u.deleted_at IS NULL ",
      nativeQuery = true
  )
  List<BigInteger> findAllUserIdByCompanyId(Long companyId);

  @Query(
      value = "SELECT phu.* FROM paid_holidays_users phu "
          + "LEFT JOIN users u "
          + "ON phu.user_id = u.id "
          + "WHERE phu.company_id = ?1 AND phu.user_id = ?2 "
          + "AND phu.deleted_at IS NULL "
          + "AND u.deleted_at IS NULL "
          + ACTIVE_USER_QUERY,
      nativeQuery = true
  )
  PaidHolidayUser findByCompanyIdAndUserId(Long companyId,Long userId);

}
