package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.PaidHolidayUser;

public interface PaidHolidayUserRepository extends BaseRepository<PaidHolidayUser, String> {
  String ACTIVE_USER_QUERY =
      "and (u.deactivated_at is null "
          + "or (u.deactivated_at is not null "
          + "and u.deactivated_at > current_timestamp)) ";

  @Query(
      value =
          "SELECT phu.* "
              + "FROM paid_holidays_users phu "
              + "LEFT JOIN users u "
              + "ON phu.user_id = u.id "
              + "WHERE phu.company_id = unhex(?1) "
              + ACTIVE_USER_QUERY,
      nativeQuery = true)
  List<PaidHolidayUser> findAllByCompanyId(String id);

  @Query(
      value =
          "SELECT hex(phu.user_id) "
              + "FROM paid_holidays_users phu "
              + "LEFT JOIN users u "
              + "ON phu.user_id = u.id "
              + "WHERE phu.company_id = unhex(?1) "
              + ACTIVE_USER_QUERY,
      nativeQuery = true)
  List<String> findAllUserIdByCompanyId(String companyId);

  @Query(
      value =
          "SELECT phu.* FROM paid_holidays_users phu "
              + "LEFT JOIN users u "
              + "ON phu.user_id = u.id "
              + "WHERE phu.company_id = unhex(?1) AND phu.user_id = unhex(?2) "
              + ACTIVE_USER_QUERY,
      nativeQuery = true)
  PaidHolidayUser findByCompanyIdAndUserId(String companyId, String userId);
}
