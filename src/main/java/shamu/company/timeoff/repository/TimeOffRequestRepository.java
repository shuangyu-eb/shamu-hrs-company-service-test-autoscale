package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.user.entity.User;

public interface TimeOffRequestRepository extends BaseRepository<TimeOffRequest, Long>,
    TimeOffRequestCustomRepository {

  List<TimeOffRequest> findByApproverUserAndTimeOffApprovalStatusIn(User approver,
      TimeOffRequestApprovalStatus[] timeOffRequestApprovalStatus);

  Integer countByApproverUserAndTimeOffApprovalStatus(User approver,
      TimeOffRequestApprovalStatus timeOffRequestApprovalStatus);

  @Query(
      value = "select * "
          + "from time_off_requests "
          + "where (approver_user_id = ?1 "
          + "or requester_user_id = ?1) "
          + "and time_off_request_approval_status_id in ( "
          + "select id "
          + "from time_off_request_approval_statuses "
          + "where name in ?2)",
      nativeQuery = true
  )
  List<TimeOffRequest> employeeFindTeamRequests(Long managerId,
      List<String> timeOffRequestApprovalStatus);

  @Query(
      value = "select * "
          + "from time_off_requests "
          + "where (approver_user_id = ?1 "
          + "or requester_user_id in (?1,?2)) "
          + "and time_off_request_approval_status_id in ( "
          + "select id "
          + "from time_off_request_approval_statuses "
          + "where name in ?3)",
      nativeQuery = true
  )
  List<TimeOffRequest> managerFindTeamRequests(Long userId, Long managerId,
      List<String> timeOffRequestApprovalStatus);

  List<TimeOffRequest> findByRequesterUserId(Long id);
}
