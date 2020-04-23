package shamu.company.timeoff.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.timeoff.pojo.TimeOffRequestDatePojo;

public interface TimeOffRequestDateRepository extends BaseRepository<TimeOffRequestDate, String> {

  @Transactional
  @Modifying
  @Query(
      value = "delete from time_off_request_dates" + " where time_off_request_id = unhex(?1)",
      nativeQuery = true)
  void deleteByTimeOffRequestId(String requestId);

  @Query(
      value =
          "SELECT "
              + "MIN(rd.date) AS startDate,"
              + "hex(request.id) AS id,"
              + "    SUM(rd.hours) AS hours "
              + "FROM "
              + "    time_off_request_dates rd "
              + "        LEFT JOIN "
              + "    time_off_requests request ON rd.time_off_request_id = request.id "
              + "        LEFT JOIN "
              + "    time_off_request_approval_statuses t "
              + "        ON request.time_off_request_approval_status_id = t.id "
              + "WHERE "
              + "    request.requester_user_id = unhex(?1) "
              + "        AND request.time_off_policy_id = unhex(?2) "
              + "        AND rd.date < ?3 "
              + "        AND t.name = ?4 "
              + "GROUP BY request.id "
              + "ORDER BY MIN(rd.date) ASC",
      nativeQuery = true)
  List<TimeOffRequestDatePojo> getTakenApprovedRequestOffByUserIdAndPolicyId(
      String userId, String policyId, LocalDateTime currentTime, String approvedStatus);

  @Query(
      value =
          "SELECT rd.date "
              + "FROM time_off_request_dates rd "
              + "WHERE rd.time_off_request_id = unhex(?1) "
              + "ORDER BY rd.created_at ASC, rd.date ",
      nativeQuery = true)
  List<Timestamp> getTimeOffRequestDatesByTimeOffRequestId(String id);
}
