package shamu.company.timeoff.repository;

import java.sql.Timestamp;
import java.util.List;
import shamu.company.timeoff.entity.TimeOffRequest;

public interface TimeOffRequestCustomRepository {

  List<Long> getFilteredReviewedTimeOffRequestsIds(Long userId, Long startTime, Long endTime);

  List<TimeOffRequest> findByTimeOffPolicyUserAndStatus(
          final Long userId, final Long policyId, final Long statusId, Timestamp currentTime);
}
