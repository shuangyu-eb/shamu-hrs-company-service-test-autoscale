package shamu.company.timeoff.request.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.request.entity.TimeOffRequest;
import shamu.company.timeoff.request.entity.TimeOffRequestApprovalStatus;
import shamu.company.user.entity.User;

public interface TimeOffRequestRepository extends BaseRepository<TimeOffRequest, Long> {

  List<TimeOffRequest> findByApproverUserAndTimeOffApprovalStatusIn(User approver,
      TimeOffRequestApprovalStatus[] timeOffRequestApprovalStatus);

  Integer countByApproverUserAndTimeOffApprovalStatus(User approver,
      TimeOffRequestApprovalStatus timeOffRequestApprovalStatus);

}
