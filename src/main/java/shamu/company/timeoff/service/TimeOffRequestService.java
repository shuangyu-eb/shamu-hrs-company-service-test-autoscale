package shamu.company.timeoff.service;

import java.util.List;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.user.entity.User;

@Service
public interface TimeOffRequestService {

  List<TimeOffRequest> getByApproverAndStatus(User approver, TimeOffRequestApprovalStatus[] status);

  List<TimeOffRequest> getByRequestersAndStatus(List<User> requsters,
      TimeOffRequestApprovalStatus status);

  Integer getCountByApproverAndStatusIsNoAction(User approver);

  TimeOffRequest getById(Long timeOffRequestId);

  TimeOffRequest save(TimeOffRequest timeOffRequest);

  TimeOffRequest createTimeOffRequest(TimeOffRequest request);

  List<TimeOffRequest> getRequestsByUserAndStatus(User user, TimeOffRequestApprovalStatus[] status);

  List<TimeOffRequest> getMyTimeOffRequestsByRequesterUserId(Long id);

  List<TimeOffRequest> getTimeOffHistories(Long userId, Long startTime, Long endTime);


  List<TimeOffRequest> getOtherTimeOffRequestsByManager(User manager);

  void sendTimeOffRequestEmail(TimeOffRequest timeOffRequest);

}
