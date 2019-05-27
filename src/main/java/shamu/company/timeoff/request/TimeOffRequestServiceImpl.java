package shamu.company.timeoff.request;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.request.entity.TimeOffRequest;
import shamu.company.timeoff.request.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.request.repository.TimeOffRequestRepository;
import shamu.company.user.entity.User;

@Service
public class TimeOffRequestServiceImpl implements TimeOffRequestService {

  private final TimeOffRequestRepository timeOffRequestRepository;

  @Autowired
  public TimeOffRequestServiceImpl(TimeOffRequestRepository timeOffRequestRepository) {
    this.timeOffRequestRepository = timeOffRequestRepository;
  }

  @Override
  public List<TimeOffRequest> getByApproverAndStatus(User approver,
      TimeOffRequestApprovalStatus[] status) {
    return timeOffRequestRepository.findByApproverUserAndTimeOffApprovalStatusIn(approver, status);
  }

  @Override
  public Integer getCountByApproverAndStatusIsNoAction(User approver) {
    return timeOffRequestRepository.countByApproverUserAndTimeOffApprovalStatus(approver,
        TimeOffRequestApprovalStatus.NO_ACTION);
  }

  @Override
  public TimeOffRequest updateTimeOffRequestsStatus() {
    return null;
  }
}
