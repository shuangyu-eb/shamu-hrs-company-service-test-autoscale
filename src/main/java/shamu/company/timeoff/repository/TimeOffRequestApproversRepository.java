package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import shamu.company.timeoff.entity.TimeOffRequestApprover;

public interface TimeOffRequestApproversRepository extends
    JpaRepository<TimeOffRequestApprover, Long> {

  @Modifying
  @Query(value = "delete from TimeOffRequestApprover ta where ta.timeOffRequestId in ?1")
  void deleteAllByTimeOffRequestIds(List<Long> timeOffRequestIds);
}
