package shamu.company.attendance.repository;

import java.util.List;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.common.repository.BaseRepository;

public interface EmployeesTaSettingRepository extends BaseRepository<EmployeesTaSetting, String> {

  @Override
  List<EmployeesTaSetting> findAll();

  EmployeesTaSetting findByEmployeeId(String userId);
}
