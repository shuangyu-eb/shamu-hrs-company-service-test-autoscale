package shamu.company.attendance.service;

import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.repository.CompanyTaSettingRepository;

/** @author mshumaker */
@Service
public class AttendanceSettingsService {
  private final CompanyTaSettingRepository companyTaSettingRepository;

  public AttendanceSettingsService(final CompanyTaSettingRepository companyTaSettingRepository) {
    this.companyTaSettingRepository = companyTaSettingRepository;
  }

  public CompanyTaSetting findCompanySettings(final String companyId) {
    return companyTaSettingRepository.findByCompanyId(companyId);
  }

  public boolean existsByCompanyId(final String companyId) {
    return companyTaSettingRepository.existsByCompanyId(companyId);
  }

  public CompanyTaSetting saveCompanyTaSetting(final CompanyTaSetting companyTaSetting) {
    return companyTaSettingRepository.save(companyTaSetting);
  }
}
