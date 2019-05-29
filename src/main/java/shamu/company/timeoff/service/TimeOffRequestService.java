package shamu.company.timeoff.service;

import java.util.List;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.user.entity.User;

@Service
public interface TimeOffRequestService {

  List<TimeOffRequest> getByApproverAndStatus(User approver, TimeOffRequestApprovalStatus[] status);

  Integer getCountByApproverAndStatusIsNoAction(User approver);

  TimeOffRequest updateTimeOffRequestsStatus();

  TimeOffRequest createTimeOffRequest(TimeOffRequest request);

  List<TimeOffRequest> getRequestsByUserAndStatus(User user, TimeOffRequestApprovalStatus[] status);
}
