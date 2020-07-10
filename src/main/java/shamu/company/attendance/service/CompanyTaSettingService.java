package shamu.company.attendance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.repository.CompanyTaSettingRepository;

@Service
public class CompanyTaSettingService {
  private final CompanyTaSettingRepository companyTaSettingRepository;

  @Autowired
  CompanyTaSettingService(final CompanyTaSettingRepository companyTaSettingRepository) {
    this.companyTaSettingRepository = companyTaSettingRepository;
  }

  public boolean existsByCompanyId(final String companyId) {
    return companyTaSettingRepository.existsByCompanyId(companyId);
  }

  public CompanyTaSetting save(final CompanyTaSetting companyTaSetting) {
    return companyTaSettingRepository.save(companyTaSetting);
  }

  public CompanyTaSetting findByCompany(final String companyId) {
    return companyTaSettingRepository.findByCompanyId(companyId);
  }
}
