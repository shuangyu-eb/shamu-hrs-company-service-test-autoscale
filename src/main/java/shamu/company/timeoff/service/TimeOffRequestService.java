package shamu.company.timeoff.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.dto.MyTimeOffDto;
import shamu.company.timeoff.dto.TimeOffRequestDetailDto;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestComment;
import shamu.company.timeoff.pojo.UnimplementedRequestPojo;
import shamu.company.user.entity.User;

@Service
public interface TimeOffRequestService {

  Page<TimeOffRequest> getByApproverAndStatusFilteredByStartDay(
      User approver, Long[] statusIds, Timestamp startDay, PageRequest pageRequest);

  Integer getPendingRequestsCount(User approver);

  TimeOffRequest getById(Long timeOffRequestId);

  TimeOffRequest save(TimeOffRequest timeOffRequest);

  TimeOffRequest createTimeOffRequest(TimeOffRequest request);

  List<TimeOffRequest> getRequestsByUserAndStatus(User user, TimeOffRequestApprovalStatus[] status);

  MyTimeOffDto getMyTimeOffRequestsByRequesterUserIdFilteredByStartDay(
      Long id, Timestamp startDay, Long[] statuses, PageRequest request);

  MyTimeOffDto getMyTimeOffRequestsByRequesterUserIdFilteredByStartAndEndDay(
      Long id, Timestamp startDay, Timestamp endDay, Long[] statuses, PageRequest request);

  MyTimeOffDto getMyTimeOffRequestsByRequesterUserId(Long id, Timestamp startDay);

  TimeOffRequestDto getMyTimeOffApprovedRequestsByRequesterUserIdAfterNow(
          Long id, Timestamp startDay, Long status);

  List<TimeOffRequest> getOtherRequestsBy(TimeOffRequest timeOffRequest);

  TimeOffRequest updateTimeOffRequest(
      TimeOffRequest timeOffRequest, TimeOffRequestComment timeOffRequestComment);

  void deleteUnimplementedRequest(
      Long requestId, UnimplementedRequestPojo unimplementedRequestPojo);

  TimeOffRequestDetailDto getTimeOffRequestDetail(Long id, Long userId);
}
