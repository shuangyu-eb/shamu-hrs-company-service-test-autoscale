package shamu.company.timeoff.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffRequestDate;

public interface TimeOffRequestDateRepository extends BaseRepository<TimeOffRequestDate, Long> {

  @Query(
      value = "select * "
          + "from time_off_request_dates "
          + "where time_off_request_id in "
          + "(select id "
          + "from time_off_requests "
          + "where time_off_policy_id in ("
          + "select id "
          + "from time_off_policies "
          + "where company_id = ?1)) "
          + "order by date",
      nativeQuery = true
  )
  List<TimeOffRequestDate> getByCompanyId(Long companyId);
}
