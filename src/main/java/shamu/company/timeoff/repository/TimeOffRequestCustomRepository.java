package shamu.company.timeoff.repository;

import java.sql.Timestamp;
import java.util.List;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestDate;

public interface TimeOffRequestCustomRepository {

  List<String> getFilteredReviewedTimeOffRequestsIds(String userId, Long startTime, Long endTime);

  List<TimeOffRequest> findByTimeOffPolicyUserAndStatus(
      final String userId, final String policyId, final TimeOffApprovalStatus status,
      Timestamp currentTime, final TimeOffRequestDate.Operator operator);
}
