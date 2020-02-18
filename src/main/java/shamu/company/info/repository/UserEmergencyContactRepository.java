package shamu.company.info.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.repository.BaseRepository;
import shamu.company.info.entity.UserEmergencyContact;

public interface UserEmergencyContactRepository
    extends BaseRepository<UserEmergencyContact, String> {

  @Query(
      value =
          "SELECT * FROM user_emergency_contacts "
              + "WHERE user_id = unhex(?1) ORDER BY is_primary DESC, last_name ASC",
      nativeQuery = true)
  List<UserEmergencyContact> findByUserId(String userId);

  @Query(
      value =
          "SELECT hex(id) FROM user_emergency_contacts "
              + "WHERE user_id = unhex(?1) ORDER BY is_primary DESC, id ASC",
      nativeQuery = true)
  List<String> findAllIdByUserId(String userId);

  @Modifying
  @Transactional
  @Query(
      value =
          "UPDATE user_emergency_contacts SET is_primary = FALSE"
              + " WHERE user_id = unhex(?1)",
      nativeQuery = true)
  void releasePrimaryContact(String userId);

  @Modifying
  @Transactional
  @Query(
      value =
          "UPDATE user_emergency_contacts SET is_primary = TRUE "
              + "WHERE user_id = unhex(?1) ORDER BY created_at Limit 1",
      nativeQuery = true)
  void resetPrimaryContact(String userId);
}
