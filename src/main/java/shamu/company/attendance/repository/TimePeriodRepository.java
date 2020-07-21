package shamu.company.attendance.repository;

import org.springframework.data.jpa.repository.Query;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheetPeriodPojo;
import shamu.company.common.repository.BaseRepository;

import java.sql.Timestamp;
import java.util.List;

public interface TimePeriodRepository extends BaseRepository<TimePeriod, String> {
  String TIME_SHEET_PERIOD_QUERY =
      "from timesheets t "
          + "join time_period tp on t.time_period_id = tp.id "
          + "where t.employee_id = unhex(?1) "
          + "order by start_date desc";

  @Query(
      value =
          "select hex(t.id) as id, tp.start_date as startDate, tp.end_date as endDate "
              + TIME_SHEET_PERIOD_QUERY,
      nativeQuery = true)
  List<TimeSheetPeriodPojo> listTimeSheetPeriodsByUser(String userId);

  @Query(value = "select tp.* " + TIME_SHEET_PERIOD_QUERY + " limit 1", nativeQuery = true)
  TimePeriod findLatestPeriodByUser(String userId);

  @Query(
      value =
          "select tp.* from time_period tp "
              + "join timesheets t "
              + "on tp.id = t. time_period_id "
              + "join users u "
              + "on u.company_id = unhex(?1) and t.employee_id = u.id "
              + "order by start_date desc limit 1",
      nativeQuery = true)
  TimePeriod findCompanyNewestPeriod(String companyId);

  TimePeriod findByStartDateAndEndDate(Timestamp startDate, Timestamp endDate);
}
