package shamu.company.timeoff.repository;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.user.entity.User;

public interface TimeOffRequestRepository
    extends BaseRepository<TimeOffRequest, Long>, TimeOffRequestCustomRepository {

  @Query(
      value =
          "select * from time_off_requests tr "
              + "where tr.id in "
              + "(select tra.time_off_request_id "
              + "from time_off_requests_approvers tra "
              + "where tra.approver_user_id = ?1) "
              + "and tr.time_off_request_approval_status_id in "
              + "(select tras.id from time_off_request_approval_statuses tras "
              + "where tras.name in ?2) "
              + "and tr.id in "
              + "(select trspan.time_off_request_id from "
              + "(select min(trd.date) startDay, max(trd.date) endDay, trd.time_off_request_id "
              + "from time_off_request_dates trd "
              + "group by trd.time_off_request_id "
              + "having startDay <= ?3 "
              + "and endDay >= ?3 "
              + "or startDay > ?3) trspan)",
      nativeQuery = true)
  List<TimeOffRequest> findByApproversAndTimeOffApprovalStatusFilteredByStartDay(
      Long approverId, List<String> statusNames, Timestamp startDay);

  @Query(
      value =
          "select t "
              + "from TimeOffRequest t "
              + "where t.timeOffApprovalStatus=?2"
              + " and t.requesterUser in ?1"
              + " and t.id in"
              + " (select td.timeOffRequestId from TimeOffRequestDate td"
              + " where td.date between ?3 and ?4"
              + " and td.deletedAt is null)"
              + " and t.deletedAt is null ")
  List<TimeOffRequest> findByRequesterUserInAndTimeOffApprovalStatus(
      List<User> requsters,
      TimeOffRequestApprovalStatus timeOffRequestApprovalStatus,
      Timestamp start,
      Timestamp end);

  Integer countByApproversContainingAndTimeOffApprovalStatusIsIn(
      User approver, TimeOffRequestApprovalStatus[] timeOffRequestApprovalStatus);

  @Query(
      value =
          "select * "
              + "from time_off_requests tr "
              + "where tr.id in (select time_off_request_id "
              + "from time_off_request_dates td "
              + "where td.date >= date_add(curdate(), INTERVAL -day(curdate())+1 day) "
              + "and td.date <= last_day(date_add(curdate(), INTERVAL +11 month))"
              + "and td.deleted_at is NULL) "
              + "and (tr.approver_user_id = ?1 "
              + "or tr.requester_user_id = ?1) "
              + "and tr.time_off_request_approval_status_id in ( "
              + "select tras.id "
              + "from time_off_request_approval_statuses tras "
              + "where tras.name in ?2)",
      nativeQuery = true)
  List<TimeOffRequest> employeeFindTeamRequests(
      Long managerId, List<String> timeOffRequestApprovalStatus);

  @Query(
      value =
          "select * "
              + "from time_off_requests tr "
              + "where tr.id in (select td.time_off_request_id "
              + "from time_off_request_dates td "
              + "where td.date >= date_add(curdate(), INTERVAL -day(curdate())+1 day) "
              + "and td.date <= last_day(date_add(curdate(), INTERVAL +11 month))"
              + "and td.deleted_at is null ) "
              + "and (tr.approver_user_id = ?1 "
              + "or tr.requester_user_id in (?1,?2) "
              + "or tr.requester_user_id in (select u.id "
              + "from users u "
              + "where u.manager_user_id = ?2)) "
              + "and tr.time_off_request_approval_status_id in ( "
              + "select tras.id "
              + "from time_off_request_approval_statuses tras "
              + "where tras.name in ?3)",
      nativeQuery = true)
  List<TimeOffRequest> managerFindTeamRequests(
      Long userId, Long managerId, List<String> timeOffRequestApprovalStatus);

  @Query(
      value =
          "select * from time_off_requests tr "
              + "where tr.id in "
              + "(select trspan.time_off_request_id from "
              + "(select min(trd.date) startDay, max(trd.date) endDay, trd.time_off_request_id "
              + "from time_off_request_dates trd "
              + "group by trd.time_off_request_id "
              + "having startDay <= ?2 "
              + "and endDay >= ?2 "
              + "or startDay > ?2 order by startDay) trspan) "
              + "and tr.requester_user_id = ?1",
      nativeQuery = true)
  List<TimeOffRequest> findByRequesterUserIdFilteredByStartDay(Long id, Timestamp startDay);

  @Query(
      value =
          "select * from time_off_requests tr "
              + "where tr.id in "
              + "(select trspan.time_off_request_id from "
              + "(select min(trd.date) startDay, max(trd.date) endDay, trd.time_off_request_id "
              + "from time_off_request_dates trd "
              + "group by trd.time_off_request_id "
              + "having startDay <= ?2 "
              + "and endDay >= ?2 "
              + "or startDay > ?2 and startDay <= ?3) trspan) "
              + "and tr.requester_user_id = ?1",
      nativeQuery = true)
  List<TimeOffRequest> findByRequesterUserIdFilteredByStartAndEndDay(
      Long id, Timestamp startDay, Timestamp endDay);

  @Query(
      value =
          "SELECT * FROM time_off_requests "
              + "WHERE deleted_at IS NULL AND time_off_policy_id = ?1",
      nativeQuery = true)
  List<TimeOffRequest> findByTimeOffPolicyId(Long timeOffPolicyId);
}
