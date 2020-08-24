package shamu.company.attendance.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.common.repository.BaseRepository;

import java.util.List;

public interface EmployeesTaSettingRepository extends BaseRepository<EmployeesTaSetting, String> {

  @Override
  List<EmployeesTaSetting> findAll();

  EmployeesTaSetting findByEmployeeId(String userId);

  @Transactional
  @Modifying
  @Query(
      value = "delete from employees_ta_settings ets where hex(ets.employee_id) in (?1) ",
      nativeQuery = true)
  void deleteAllByUserId(List<String> ids);
}
