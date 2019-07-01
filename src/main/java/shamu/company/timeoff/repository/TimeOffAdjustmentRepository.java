package shamu.company.timeoff.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shamu.company.timeoff.entity.TimeOffAdjustment;

public interface TimeOffAdjustmentRepository extends
    JpaRepository<TimeOffAdjustment, Long> {

}
