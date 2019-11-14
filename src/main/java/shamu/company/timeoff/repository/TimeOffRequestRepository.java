package shamu.company.timeoff.repository;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.pojo.TimeOffRequestStatusPojo;
import shamu.company.user.entity.User;

public interface TimeOffRequestRepository
    extends BaseRepository<TimeOffRequest, String>, TimeOffRequestCustomRepository {
  // TODO unhex in list ?2
  @Query(
      value =
          "select * from time_off_requests tr "
              + "left join time_off_requests_approvers tra "
              + "   on tra.time_off_request_id = tr.id "
              + "left join time_off_request_approval_statuses tras "
              + "   on tr.time_off_request_approval_status_id = tras.id "
              + "where tra.approver_user_id = unhex(?1) "
              + " and tras.name in ?2"
              + " and tr.id in "
              + "   (select trspan.time_off_request_id from "
              + "            (select min(trd.date) startDay, max(trd.date) endDay, "
              + "               trd.time_off_request_id "
              + "             from time_off_request_dates trd "
              + "             group by trd.time_off_request_id "
              + "             having startDay <= ?3 "
              + "             and endDay >= ?3 "
              + "             or startDay > ?3) trspan)",
      nativeQuery = true)
  Page<TimeOffRequest> findByApproversAndTimeOffApprovalStatusFilteredByStartDay(
      String approverId, String[] statuses, Timestamp startDay, PageRequest pageRequest);

  @Query(
      value =
          "select * from time_off_requests t "
              + " left join time_off_request_approval_statuses trs "
              + " on t.time_off_request_approval_status_id = trs.id "
              + " where trs.name = ?2 and t.requester_user_id in ?1 "
              + "   and t.id in ("
              + "     select td.time_off_request_id from time_off_request_dates td "
              + "       where td.date between ?3 and ?4)", nativeQuery = true)
  List<TimeOffRequest> findByRequesterUserInAndTimeOffApprovalStatus(
      List<byte[]> requsters, String approvalStatus, Timestamp start, Timestamp end);

  @Query(value = "select count(1) from time_off_requests tr "
      + " left join time_off_request_approval_statuses trs "
      + "    on tr.time_off_request_approval_status_id = trs.id"
      + " where tr.approver_user_id = unhex(?1) and trs.name in ?2", nativeQuery = true)
  Integer countByApproversContainingAndTimeOffApprovalStatusIsIn(
      String approver, String[] timeOffRequestApprovalStatus);

  // TODO test entity transform with native query
  @Query(
      value =
              "SELECT tr.* FROM time_off_requests tr "
                  + "LEFT JOIN time_off_request_dates td "
                  + "   ON tr.id = td.time_off_request_id "
                  + "LEFT JOIN users u "
                  + "   ON tr.requester_user_id = u.id "
                  + "LEFT JOIN time_off_request_approval_statuses tras "
                  + "   ON tr.time_off_request_approval_status_id = tras.id "
                  + "WHERE (tr.requester_user_id = unhex(?1) "
                  + "   OR u.manager_user_id = unhex(?1)) "
                  + "   and tras.name in ?2 "
                  + "group by tr.id ",
      nativeQuery = true)
  List<TimeOffRequest> employeeFindTeamRequests(
      String managerId, List<String> timeOffRequestApprovalStatus);

  // TODO replace hard encode
  @Query(
      value =
          "SELECT tr.* FROM time_off_requests tr "
              + "LEFT JOIN time_off_request_dates td "
              + "   ON tr.id = td.time_off_request_id "
              + "LEFT JOIN users u "
              + "   ON tr.requester_user_id = u.id "
              + "LEFT JOIN time_off_request_approval_statuses tras "
              + "   ON tras.name = ?2 "
              + "WHERE tr.requester_user_id = unhex(?1) "
              + "   and tras.id = 1 "
              + "group by tr.id ",
      nativeQuery = true)
  List<TimeOffRequest> employeeFindSelfPendingRequests(
      String employeeId, String statusName);

  @Query(
      value =
          "SELECT tr.* FROM time_off_requests tr "
              + "LEFT JOIN time_off_request_dates td "
              + "   ON tr.id = td.time_off_request_id "
              + "LEFT JOIN users u "
              + "   ON tr.requester_user_id = u.id "
              + "LEFT JOIN time_off_request_approval_statuses tras "
              + "   ON tr.time_off_request_approval_status_id = tras.id "
              + "WHERE (tr.requester_user_id IN (unhex(?1), unhex(?2)) "
              + "   OR u.manager_user_id IN (unhex(?1), unhex(?2))) "
              + "   AND tras.name in ?3 "
              + "group by tr.id ",
      nativeQuery = true)
  List<TimeOffRequest> managerFindTeamRequests(
      String userId, String managerId, List<String> timeOffRequestApprovalStatus);

  @Query(
      value =
          "SELECT tr.* FROM time_off_requests tr "
              + "LEFT JOIN time_off_request_dates td "
              + "   ON tr.id = td.time_off_request_id "
              + "LEFT JOIN users u "
              + "   ON tr.requester_user_id = u.id "
              + "LEFT JOIN time_off_request_approval_statuses tras "
              + "   ON tr.time_off_request_approval_status_id = tras.id "
              + "WHERE (tr.requester_user_id = unhex(?1) OR u.manager_user_id = unhex(?1)) "
              + "   and tras.name in ?2 "
              + "group by tr.id ",
      nativeQuery = true)
  List<TimeOffRequest> adminFindTeamRequests(
      String userId, List<String> timeOffRequestApprovalStatus);

  // TODO replace hard status id
  @Query(
      value = "select * from time_off_requests tr "
          + "left join time_off_request_approval_statuses tras "
          + "on tr.time_off_request_approval_status_id = tras.id "
          + "where tr.id in "
          + "  (select trspan.time_off_request_id from "
          + "    (select min(trd.date) startDay, max(trd.date) endDay, trd.time_off_request_id "
          + "       from time_off_request_dates trd "
          + "       group by trd.time_off_request_id "
          + "       having startDay <= ?2 "
          + "       and endDay >= ?2 "
          + "        or startDay > ?2) trspan) "
          + "and tr.requester_user_id = unhex(?1) "
          + "and tras.name in ?3 ",
      nativeQuery = true)
  Page<TimeOffRequest> findByRequesterUserIdFilteredByStartDay(
      String id, Timestamp startDay, String[] statuses, PageRequest pageRequest);

  // TODO replace hard status id
  @Query(
      value =
        "select * from time_off_requests tr "
          + "left join time_off_request_approval_statuses tras "
          + "on tr.time_off_request_approval_status_id = tras.id "
          + "where tr.id in "
          + "(select trspan.time_off_request_id from "
          + "(select min(trd.date) startDay, max(trd.date) endDay, trd.time_off_request_id "
          + "from time_off_request_dates trd "
          + "group by trd.time_off_request_id "
          + "having startDay <= ?2 "
          + "and endDay >= ?2 "
          + "or startDay > ?2 and startDay <= ?3) trspan) "
          + "and tr.requester_user_id = unhex(?1) "
          + "and tras.name in ?4",
      nativeQuery = true)
  Page<TimeOffRequest> findByRequesterUserIdFilteredByStartAndEndDay(
      String id, Timestamp startDay, Timestamp endDay, String[] statuses, PageRequest request);

  @Query(
      value = "select * from time_off_requests tr "
        + "where tr.id in "
        + "  (select trspan.time_off_request_id from "
        + "    (select min(trd.date) startDay, max(trd.date) endDay, trd.time_off_request_id "
        + "       from time_off_request_dates trd "
        + "       group by trd.time_off_request_id "
        + "       having startDay <= ?2 "
        + "       and endDay >= ?2 "
        + "        or startDay > ?2) trspan) "
        + "and tr.requester_user_id = unhex(?1) ",
      nativeQuery = true
  )
  List<TimeOffRequest> findByRequesterUserIdFilteredByStartDayWithoutPaging(
      String id, Timestamp startDay
  );

  // TODO status id
  @Query(
          value =
                  "select tr.* from time_off_requests tr "
                      + "join time_off_request_dates tord on tr.id = tord.time_off_request_id "
                      + "join time_off_request_approval_statuses tras "
                      + " on tr.time_off_request_approval_status_id = tras.id "
                      + "where tras.name = ?3 "
                      + "and tr.requester_user_id = unhex(?1) "
                      + "and tord.DATE > ?2 "
                      + "order by tord.date , tr.created_at limit 1 ",
          nativeQuery = true)
  TimeOffRequest findRecentApprovedRequestByRequesterUserId(
      String id, Timestamp startDay, String status);

  @Query("SELECT new shamu.company.timeoff.pojo.TimeOffRequestStatusPojo "
      + "(tr.id, tr.timeOffApprovalStatus) FROM TimeOffRequest tr"
              + " WHERE tr.timeOffPolicy.id = ?1")
  List<TimeOffRequestStatusPojo> findByTimeOffPolicyId(String timeOffPolicyId);
}
