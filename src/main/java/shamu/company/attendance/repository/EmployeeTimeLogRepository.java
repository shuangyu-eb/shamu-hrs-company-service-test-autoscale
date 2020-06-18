package shamu.company.attendance.repository;

import java.util.List;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.common.repository.BaseRepository;

public interface EmployeeTimeLogRepository extends BaseRepository<EmployeeTimeLog, String> {
    List<EmployeeTimeLog> findAllByEntryId(String entryId);
}
