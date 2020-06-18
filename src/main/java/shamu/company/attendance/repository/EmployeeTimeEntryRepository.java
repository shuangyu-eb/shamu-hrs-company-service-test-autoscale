package shamu.company.attendance.repository;

import java.util.List;
import shamu.company.attendance.entity.EmployeeTimeEntry;
import shamu.company.common.repository.BaseRepository;

public interface EmployeeTimeEntryRepository extends BaseRepository<EmployeeTimeEntry, String> {
    List<EmployeeTimeEntry> findAllByTimesheetId(String timeSheetId);
}
