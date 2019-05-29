package shamu.company.timeoff.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.repository.TimeOffRequestRepository;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserRole.Role;

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

  @Override
  public TimeOffRequest createTimeOffRequest(TimeOffRequest request) {
    return timeOffRequestRepository.save(request);
  }

  @Override
  public List<TimeOffRequest> getRequestsByUserAndStatus(User user,
      TimeOffRequestApprovalStatus[] status) {
    List<TimeOffRequestApprovalStatus> statusList = Arrays.asList(status);
    List<String> statusNames = statusList.stream().map(element -> element.name())
        .collect(Collectors.toList());

    if (user.getRole().name().equals(Role.NON_MANAGER.name())) {
      return timeOffRequestRepository
          .employeeFindTeamRequests(user.getManagerUser().getId(), statusNames);
    } else if (user.getRole().name().equals(Role.MANAGER.name())) {
      return timeOffRequestRepository
          .managerFindTeamRequests(user.getId(), user.getManagerUser().getId(),
              statusNames);
    } else {
      return timeOffRequestRepository
          .managerFindTeamRequests(user.getId(), null,
              statusNames);
    }
  }
}
