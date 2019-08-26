package shamu.company.timeoff.repository;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
              + "where tr.deleted_at is null "
              + "and tr.id in "
              + "(select tra.time_off_request_id "
              + "from time_off_requests_approvers tra "
              + "where tra.approver_user_id = ?1) "
              + "and tr.time_off_request_approval_status_id in ?2 "
              + "and tr.id in "
              + "(select trspan.time_off_request_id from "
              + "(select min(trd.date) startDay, max(trd.date) endDay, trd.time_off_request_id "
              + "from time_off_request_dates trd "
              + "group by trd.time_off_request_id "
              + "having startDay <= ?3 "
              + "and endDay >= ?3 "
              + "or startDay > ?3) trspan)",
      nativeQuery = true)
  Page<TimeOffRequest> findByApproversAndTimeOffApprovalStatusFilteredByStartDay(
      Long approverId, Long[] statusIds, Timestamp startDay, PageRequest pageRequest);

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
              "SELECT tr.* FROM time_off_requests tr "
                  + "LEFT JOIN time_off_request_dates td "
                  + "   ON tr.id = td.time_off_request_id "
                  + "LEFT JOIN users u "
                  + "   ON tr.requester_user_id = u.id "
                  + "LEFT JOIN time_off_request_approval_statuses tras "
                  + "   ON tr.time_off_request_approval_status_id = tras.id "
                  + "WHERE (tr.requester_user_id = ?1 "
                  + "   OR u.manager_user_id = ?1) "
                  + "   and tr.deleted_at IS NULL "
                  + "   and tras.name in ?2 "
                  + "group by tr.id "
                  + "   having min(td.date) >= date_add(curdate(), INTERVAL -day(curdate())+1 day) "
                  + "       and max(td.date) <= last_day(date_add(curdate(), INTERVAL +11 month)) ",
      nativeQuery = true)
  List<TimeOffRequest> employeeFindTeamRequests(
      Long managerId, List<String> timeOffRequestApprovalStatus);

  @Query(
      value =
          "SELECT tr.* FROM time_off_requests tr "
              + "LEFT JOIN time_off_request_dates td "
              + "   ON tr.id = td.time_off_request_id "
              + "LEFT JOIN users u "
              + "   ON tr.requester_user_id = u.id "
              + "LEFT JOIN time_off_request_approval_statuses tras "
              + "   ON tr.time_off_request_approval_status_id = tras.id "
              + "WHERE (tr.requester_user_id IN (?1, ?2) "
              + "   OR u.manager_user_id IN (?1, ?2)) "
              + "tr.deleted_at is null "
              + "   AND tras.name in ?3 "
              + "group by tr.id "
              + "   having min(td.date) >= date_add(curdate(), INTERVAL -day(curdate())+1 day) "
              + "       and max(td.date) <= last_day(date_add(curdate(), INTERVAL +11 month)) ",
      nativeQuery = true)
  List<TimeOffRequest> managerFindTeamRequests(
      Long userId, Long managerId, List<String> timeOffRequestApprovalStatus);

  @Query(
      value =
          "SELECT tr.* FROM time_off_requests tr "
              + "LEFT JOIN time_off_request_dates td "
              + "   ON tr.id = td.time_off_request_id "
              + "LEFT JOIN users u "
              + "   ON tr.requester_user_id = u.id "
              + "LEFT JOIN time_off_request_approval_statuses tras "
              + "   ON tr.time_off_request_approval_status_id = tras.id "
              + "WHERE (tr.requester_user_id = ?1 OR u.manager_user_id = ?1) "
              + "   and tras.name in ?2 "
              + "   and tr.deleted_at is null "
              + "group by tr.id "
              + "   having min(td.date) >= date_add(curdate(), INTERVAL -day(curdate())+1 day) "
              + "       and max(td.date) <= last_day(date_add(curdate(), INTERVAL +11 month)) ",
      nativeQuery = true)
  List<TimeOffRequest> adminFindTeamRequests(
      Long userId, List<String> timeOffRequestApprovalStatus);

  @Query(
      value = "select * from time_off_requests tr "
        + "where tr.deleted_at is null "
        + "and tr.id in "
        + "  (select trspan.time_off_request_id from "
        + "    (select min(trd.date) startDay, max(trd.date) endDay, trd.time_off_request_id "
        + "       from time_off_request_dates trd "
        + "       group by trd.time_off_request_id "
        + "       having startDay <= ?2 "
        + "       and endDay >= ?2 "
        + "        or startDay > ?2) trspan) "
        + "and tr.requester_user_id = ?1 "
        + "and tr.time_off_request_approval_status_id in ?3 ",
      nativeQuery = true)
  Page<TimeOffRequest> findByRequesterUserIdFilteredByStartDay(
      Long id, Timestamp startDay, Long[] statuses, PageRequest pageRequest);


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
          + "and tr.requester_user_id = ?1 "
          + "and tr.time_off_request_approval_status_id in ?4",
      nativeQuery = true)
  Page<TimeOffRequest> findByRequesterUserIdFilteredByStartAndEndDay(
      Long id, Timestamp startDay, Timestamp endDay, Long[] statuses, PageRequest request);

  @Query(
      value = "select * from time_off_requests tr "
        + "where tr.deleted_at is null "
        + "and tr.id in "
        + "  (select trspan.time_off_request_id from "
        + "    (select min(trd.date) startDay, max(trd.date) endDay, trd.time_off_request_id "
        + "       from time_off_request_dates trd "
        + "       group by trd.time_off_request_id "
        + "       having startDay <= ?2 "
        + "       and endDay >= ?2 "
        + "        or startDay > ?2) trspan) "
        + "and tr.requester_user_id = ?1 ",
      nativeQuery = true
  )
  List<TimeOffRequest> findByRequesterUserIdFilteredByStartDayWithoutPaging(
      Long id, Timestamp startDay
  );

  @Query(
          value =
                  "select * from time_off_requests tr "
                          + "where tr.deleted_at is null "
                          + "and tr.time_off_request_approval_status_id = ?3 "
                          + "and tr.id in (select trspan.time_off_request_id from "
                          + "(select min(trd.date) startDay, trd.time_off_request_id "
                          + "from time_off_request_dates trd "
                          + "group by trd.time_off_request_id "
                          + "having startDay > ?2) trspan) "
                          + "and tr.requester_user_id = ?1 limit 1 ",
          nativeQuery = true)
  TimeOffRequest findByRequesterUserIdFilteredByApprovedAndStartDay(
          Long id, Timestamp startDay, Long status);

  @Query(
      value =
          "SELECT * FROM time_off_requests "
              + "WHERE deleted_at IS NULL AND time_off_policy_id = ?1",
      nativeQuery = true)
  List<TimeOffRequest> findByTimeOffPolicyId(Long timeOffPolicyId);
}
