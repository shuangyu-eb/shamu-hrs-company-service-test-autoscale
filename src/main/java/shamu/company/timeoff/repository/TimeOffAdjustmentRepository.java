package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shamu.company.timeoff.entity.TimeOffAdjustment;

public interface TimeOffAdjustmentRepository extends JpaRepository<TimeOffAdjustment, Long> {

  @Query(value = "SELECT "
      + "    *"
      + "FROM "
      + "    time_off_adjustments tf "
      + "WHERE"
      + "    tf.user_id = ?1 "
      + "        AND tf.time_off_policy_id = ?2 "
      + "ORDER BY tf.created_at ASC; ", nativeQuery = true)
  List<TimeOffAdjustment> findAllByUserIdAndTimeOffPolicyId(Long userId, Long policyId);
}
