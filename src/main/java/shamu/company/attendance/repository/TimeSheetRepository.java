package shamu.company.attendance.repository;

import org.springframework.data.jpa.repository.Query;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.common.repository.BaseRepository;

import java.util.List;

public interface TimeSheetRepository extends BaseRepository<TimeSheet, String> {

  @Query(
      value =
          "select t.* from timesheets t join users u  "
              + "on u.id = t.employee_id and u.company_id = unhex(?1)",
      nativeQuery = true)
  List<TimeSheet> listByCompanyId(String companyId);
}
