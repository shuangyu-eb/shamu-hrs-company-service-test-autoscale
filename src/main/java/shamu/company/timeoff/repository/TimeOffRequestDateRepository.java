package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.timeoff.pojo.TimeOffRequestDatePojo;

public interface TimeOffRequestDateRepository extends BaseRepository<TimeOffRequestDate, Long> {

  @Query(
      value =
          "select * "
              + "from time_off_request_dates "
              + "where time_off_request_id in "
              + "(select id "
              + "from time_off_requests "
              + "where time_off_policy_id in ("
              + "select id "
              + "from time_off_policies "
              + "where company_id = ?1)) "
              + "order by date",
      nativeQuery = true)
  List<TimeOffRequestDate> getByCompanyId(Long companyId);

  @Query(
      value =
          "select * "
              + "from time_off_request_dates "
              + "where time_off_request_id in "
              + "(select id "
              + "from time_off_requests "
              + "where requester_user_id = ?1) "
              + "order by date",
      nativeQuery = true)
  List<TimeOffRequestDate> getByRequesterUserId(Long userId);

  @Transactional
  @Modifying
  @Query(
      value =
          "update time_off_request_dates"
              + " set deleted_at = current_timestamp"
              + " where time_off_request_id = ?1"
              + " and deleted_at is null ",
      nativeQuery = true)
  void deleteByTimeOffRequestId(Long requestId);

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
      + "    rd.deleted_at IS NULL "
      + "        AND request.deleted_at IS NULL "
      + "        AND t.name != 'DENIED' "
      + "        AND request.requester_user_id = ?1 "
      + "        AND request.time_off_policy_id = ?2 "
      + "GROUP BY request.id "
      + "ORDER BY createDate ASC",
      nativeQuery = true)
  List<TimeOffRequestDatePojo> getNoRejectedRequestOffByUserIdAndPolicyId(
      Long userId, Long policyId);
}
