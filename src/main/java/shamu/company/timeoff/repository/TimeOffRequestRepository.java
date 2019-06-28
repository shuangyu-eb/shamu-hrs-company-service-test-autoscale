package shamu.company.timeoff.repository;

import java.sql.Timestamp;
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

  @Query("select t "
      + "from TimeOffRequest t left join TimeOffRequestDate td on td.timeOffRequestId=t.id "
      + "where t.timeOffApprovalStatus=?2 and t.requesterUser in ?1 and td.date between ?3 and ?4")
  List<TimeOffRequest> findByRequesterUserInAndTimeOffApprovalStatus(List<User> requsters,
      TimeOffRequestApprovalStatus timeOffRequestApprovalStatus, Timestamp start, Timestamp end);

  Integer countByApproverUserAndTimeOffApprovalStatus(User approver,
      TimeOffRequestApprovalStatus timeOffRequestApprovalStatus);

  @Query(
      value = "select * "
          + "from time_off_requests tr "
          + "where tr.id in (select time_off_request_id "
          + "from time_off_request_dates td "
          + "where td.date >= date_add(curdate(), INTERVAL -day(curdate())+1 day) "
          + "and td.date <= last_day(date_add(curdate(), INTERVAL +11 month))) "
          + "and (approver_user_id = ?1 "
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
          + "from time_off_requests tr "
          + "where tr.id in (select time_off_request_id "
          + "from time_off_request_dates td "
          + "where td.date >= date_add(curdate(), INTERVAL -day(curdate())+1 day) "
          + "and td.date <= last_day(date_add(curdate(), INTERVAL +11 month))) "
          + "and (approver_user_id = ?1 "
          + "or requester_user_id in (?1,?2) "
          + "or requester_user_id in (select id "
          + "from users "
          + "where manager_user_id = ?2)) "
          + "and time_off_request_approval_status_id in ( "
          + "select id "
          + "from time_off_request_approval_statuses "
          + "where name in ?3)",
      nativeQuery = true
  )
  List<TimeOffRequest> managerFindTeamRequests(Long userId, Long managerId,
      List<String> timeOffRequestApprovalStatus);

  List<TimeOffRequest> findByRequesterUserId(Long id);

  @Query(
      value = "SELECT * FROM time_off_requests "
          + "WHERE deleted_at IS NULL AND time_off_policy_id = ?1",
      nativeQuery = true)
  List<TimeOffRequest> findByTimeOffPolicyId(Long timeOffPolicyId);
}
