package shamu.company.timeoff.service;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.dto.MyTimeOffDto;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestComment;
import shamu.company.timeoff.pojo.UnimplementedRequestPojo;
import shamu.company.user.entity.User;

@Service
public interface TimeOffRequestService {

  List<TimeOffRequest> getByApproverAndStatus(
      User approver, TimeOffRequestApprovalStatus[] status, Timestamp startDay, Timestamp endDay);

  Integer getCountByApproverAndStatusIsNoAction(User approver);

  TimeOffRequest getById(Long timeOffRequestId);

  TimeOffRequest save(TimeOffRequest timeOffRequest);

  TimeOffRequest createTimeOffRequest(TimeOffRequest request);

  List<TimeOffRequest> getRequestsByUserAndStatus(User user, TimeOffRequestApprovalStatus[] status);

  MyTimeOffDto getMyTimeOffRequestsByRequesterUserId(Long id, Timestamp startDay, Timestamp endDay);

  List<TimeOffRequest> getOtherRequestsBy(TimeOffRequest timeOffRequest);

  void sendTimeOffRequestEmail(TimeOffRequest timeOffRequest);

  TimeOffRequest updateTimeOffRequest(
      TimeOffRequest timeOffRequest, TimeOffRequestComment timeOffRequestComment);

  List<TimeOffRequest> getTimeOffRequestsByTimeOffPolicyId(Long id);

  void deleteUnimplementedRequest(
      Long requestId, UnimplementedRequestPojo unimplementedRequestPojo);
}
