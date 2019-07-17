package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.dto.TimeOffRequestDateDto;
import shamu.company.timeoff.entity.TimeOffRequestDate;

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

  @Query(value = "select rd.date as date, rd.hours as hours from time_off_request_dates rd "
      + "left join time_off_requests request on rd.time_off_request_id = request.id "
      + "left join time_off_request_approval_statuses t "
      + "on request.time_off_request_approval_status_id = t.id "
      + "where rd.deleted_at is null and request.deleted_at is null "
      + "and t.name != 'DENIED' "
      + "and request.requester_user_id = ?1 and request.time_off_policy_id = ?2",
      nativeQuery = true)
  List<TimeOffRequestDateDto> getNoRejectedRequestOffByUserIdAndPolicyId(
      Long userId, Long policyId);
}
