package shamu.company.attendance.service;

import org.springframework.stereotype.Service;
import shamu.company.attendance.repository.CompanyTaSettingRepository;

@Service
public class AttendanceSetUpService {

  private final CompanyTaSettingRepository companyTaSettingRepository;

  public AttendanceSetUpService(CompanyTaSettingRepository companyTaSettingRepository) {
    this.companyTaSettingRepository = companyTaSettingRepository;
  }

  public Boolean findIsAttendanceSetUp(final String companyId) {
    return companyTaSettingRepository.existsByCompanyId(companyId);
  }
}
