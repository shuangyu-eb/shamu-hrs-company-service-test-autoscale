package shamu.company.attendance.service;

import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.attendance.repository.EmployeesTaSettingRepository;

import java.util.List;

@Service
public class EmployeesTaSettingService {
  private final EmployeesTaSettingRepository employeesTaSettingRepository;

  public EmployeesTaSettingService(
      final EmployeesTaSettingRepository employeesTaSettingRepository) {
    this.employeesTaSettingRepository = employeesTaSettingRepository;
  }

  public EmployeesTaSetting findByUserId(final String userId) {
    return employeesTaSettingRepository.findByEmployeeId(userId);
  }

  public boolean isEnrolledInTA(final String userId) {
    final EmployeesTaSetting employeesTaSetting = findByUserId(userId);
    return null != employeesTaSetting;
  }

  public void removeEmployees(final List<String> userIds) {
    employeesTaSettingRepository.deleteAllByUserId(userIds);
  }
}
