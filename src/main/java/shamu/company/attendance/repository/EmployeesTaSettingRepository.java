package shamu.company.attendance.repository;

import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.common.repository.BaseRepository;

import java.util.List;

public interface EmployeesTaSettingRepository extends BaseRepository<EmployeesTaSetting, String> {

    List<EmployeesTaSetting> findAll();
}
