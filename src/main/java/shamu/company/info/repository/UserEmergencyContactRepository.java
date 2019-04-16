package shamu.company.info.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.repository.BaseRepository;
import shamu.company.info.entity.UserEmergencyContact;

public interface UserEmergencyContactRepository extends BaseRepository<UserEmergencyContact, Long> {

  @Query(
      value = "SELECT * FROM user_emergency_contacts "
          + "WHERE deleted_at IS NULL AND user_id = ?1 ORDER BY updated_at DESC",
      nativeQuery = true)
  List<UserEmergencyContact> findByUserId(Long id);

  @Modifying
  @Transactional
  @Query(
      value = "UPDATE user_emergency_contacts SET is_primary = FALSE"
          + " WHERE deleted_at IS NULL AND user_id = ?1",
      nativeQuery = true)
  void releasePrimaryContact(Long id);

  @Modifying
  @Transactional
  @Query(
      value = "UPDATE user_emergency_contacts SET is_primary = TRUE "
          + "WHERE deleted_at IS NULL AND user_id = ?1 ORDER BY id Limit 1",
      nativeQuery = true)
  void resetPrimaryContact(Long id);
}
