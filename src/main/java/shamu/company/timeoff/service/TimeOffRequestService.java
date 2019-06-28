package shamu.company.timeoff.service;

import java.util.List;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.dto.MyTimeOffDto;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.user.entity.User;

@Service
public interface TimeOffRequestService {

  List<TimeOffRequest> getByApproverAndStatus(User approver, TimeOffRequestApprovalStatus[] status);

  Integer getCountByApproverAndStatusIsNoAction(User approver);

  TimeOffRequest getById(Long timeOffRequestId);

  TimeOffRequest save(TimeOffRequest timeOffRequest);

  TimeOffRequest createTimeOffRequest(TimeOffRequest request);

  List<TimeOffRequest> getRequestsByUserAndStatus(User user, TimeOffRequestApprovalStatus[] status);

  MyTimeOffDto getMyTimeOffRequestsByRequesterUserId(Long id);

  List<TimeOffRequest> getTimeOffHistories(Long userId, Long startTime, Long endTime);


  List<TimeOffRequest> getOtherRequestsBy(TimeOffRequest timeOffRequest);

  void sendTimeOffRequestEmail(TimeOffRequest timeOffRequest);

  TimeOffRequest updateTimeOffRequest(TimeOffRequest timeOffRequest);

  List<TimeOffRequest> getTimeOffRequestsByTimeOffPolicyId(Long id);

}
