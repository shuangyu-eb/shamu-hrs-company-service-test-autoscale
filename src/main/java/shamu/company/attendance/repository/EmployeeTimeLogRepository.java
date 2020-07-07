package shamu.company.attendance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.common.repository.BaseRepository;

public interface EmployeeTimeLogRepository extends BaseRepository<EmployeeTimeLog, String> {
  List<EmployeeTimeLog> findAllByEntryId(String entryId);

  @Query(
      value =
          "select etl from EmployeeTimeLog etl "
              + "left join EmployeeTimeEntry ete on etl.entry.id = ete.id "
              + "left join TimeSheet ts on ts.id = ete.timesheet.id "
              + "where ts.id = ?1 and etl.timeType.name = ?2")
  List<EmployeeTimeLog> findWorkTimeLog(String timeSheetId, String typeName);

  @Query(
      value =
          "select etl.* from employee_time_logs etl "
              + "left join employee_time_entries ete on etl.entry_id = ete.id "
              + "where ete.employee_id = unhex(?3) and "
              + "UNIX_TIMESTAMP(etl.start) < ?2 and "
              + "(UNIX_TIMESTAMP(etl.start) + etl.duration_min * 60) > ?1",
      nativeQuery = true)
  List<EmployeeTimeLog> findEmployeeTimeLogByTime(long start, long end, String userId);
}
