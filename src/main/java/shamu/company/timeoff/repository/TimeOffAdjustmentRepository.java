package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import shamu.company.timeoff.entity.TimeOffAdjustment;

public interface TimeOffAdjustmentRepository extends JpaRepository<TimeOffAdjustment, Long> {

  List<TimeOffAdjustment> findAllByUserIdAndAndTimeOffPolicyId(Long userId, Long policyId);
}
