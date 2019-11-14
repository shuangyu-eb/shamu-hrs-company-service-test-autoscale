package shamu.company.timeoff.repository;

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
      value =
          "delete from time_off_request_dates"
              + " where time_off_request_id = unhex(?1)",
      nativeQuery = true)
  void deleteByTimeOffRequestId(String requestId);

  @Query(value = "SELECT "
      + "request.created_at AS createDate,"
      + "    MIN(rd.date) AS startDate,"
      + "    MAX(rd.date) AS endDate,"
      + "    SUM(rd.hours) AS hours "
      + "FROM "
      + "    time_off_request_dates rd "
      + "        LEFT JOIN "
      + "    time_off_requests request ON rd.time_off_request_id = request.id "
      + "        LEFT JOIN "
      + "    time_off_request_approval_statuses t "
      + "        ON request.time_off_request_approval_status_id = t.id "
      + "WHERE "
      + "    t.name = 'APPROVED' "
      + "        AND request.requester_user_id = unhex(?1) "
      + "        AND request.time_off_policy_id = unhex(?2) "
      + "        AND rd.date < ?3 "
      + "GROUP BY request.id "
      + "ORDER BY createDate ASC",
      nativeQuery = true)
  List<TimeOffRequestDatePojo> getTakenApprovedRequestOffByUserIdAndPolicyId(
      String userId, String policyId, LocalDateTime currentTime);
}
