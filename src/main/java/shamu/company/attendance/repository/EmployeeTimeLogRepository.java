package shamu.company.attendance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.common.repository.BaseRepository;

public interface EmployeeTimeLogRepository extends BaseRepository<EmployeeTimeLog, String> {

  @Query(
      value =
          "select etl.* from employee_time_logs etl "
              + "left join employee_time_entries ete on etl.entry_id = ete.id "
              + "where ete.employee_id = unhex(?3) and "
              + "UNIX_TIMESTAMP(etl.start) * 1000 < ?2 and "
              + "(UNIX_TIMESTAMP(etl.start) * 1000 + etl.duration_min * 60 * 1000) > ?1",
      nativeQuery = true)
  List<EmployeeTimeLog> findEmployeeTimeLogByTime(long start, long end, String userId);

  List<EmployeeTimeLog> findAllByEntryId(String entryId);
}
