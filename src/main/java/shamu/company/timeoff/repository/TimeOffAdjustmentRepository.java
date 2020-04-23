package shamu.company.timeoff.repository;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shamu.company.timeoff.entity.TimeOffAdjustment;
import shamu.company.timeoff.pojo.TimeOffAdjustmentPojo;

public interface TimeOffAdjustmentRepository extends JpaRepository<TimeOffAdjustment, String> {

  @Query(
      "SELECT new "
          + "shamu.company.timeoff.pojo.TimeOffAdjustmentPojo(tf.createdAt, tf.amount, tf.comment) "
          + "FROM TimeOffAdjustment tf "
          + "WHERE tf.user.id = ?1 "
          + "AND tf.timeOffPolicy.id = ?2 "
          + "AND tf.createdAt <= ?3 "
          + "ORDER BY tf.createdAt ASC")
  List<TimeOffAdjustmentPojo> findAllByUserIdAndTimeOffPolicyId(
      String userId, String policyId, Date endDate);
}
