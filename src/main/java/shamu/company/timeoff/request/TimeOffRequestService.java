package shamu.company.timeoff.request;

import java.util.List;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.request.entity.TimeOffRequest;
import shamu.company.timeoff.request.entity.TimeOffRequestApprovalStatus;
import shamu.company.user.entity.User;

@Service
public interface TimeOffRequestService {

  List<TimeOffRequest> getByApproverAndStatus(User approver, TimeOffRequestApprovalStatus[] status);

  Integer getCountByApproverAndStatusIsNoAction(User approver);

  TimeOffRequest updateTimeOffRequestsStatus();

}
