package shamu.company.attendance.repository;

import org.springframework.data.jpa.repository.Query;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.common.repository.BaseRepository;

import java.util.List;

public interface TimePeriodRepository extends BaseRepository<TimePeriod, String> {
  @Query(
      value =
          "select tp.* from timesheets t "
              + "join time_period tp on t.time_period_id = tp.id where t.employee_id = unhex(?1) order by start_date desc",
      nativeQuery = true)
  List<TimePeriod> listTimePeriodsByUser(String userId);

  @Query(
      value =
          "select t.* from timesheets t, users u "
              + "where u.company_id = unhex(?1) and t.employee_id = u.id order by start_date desc limit 1",
      nativeQuery = true)
  TimePeriod findCompanyNewestPeriod(String companyId);
}
