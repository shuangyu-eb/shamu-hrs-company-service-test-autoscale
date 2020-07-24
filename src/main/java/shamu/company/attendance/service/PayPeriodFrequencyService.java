package shamu.company.attendance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.repository.PayPeriodFrequencyRepository;
import shamu.company.common.entity.PayrollDetail;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.service.PayrollDetailService;

import java.util.List;
import java.util.Optional;

@Service
public class PayPeriodFrequencyService {

  private final PayPeriodFrequencyRepository payPeriodFrequencyRepository;
  private final PayrollDetailService payrollDetailService;

  @Autowired
  public PayPeriodFrequencyService(
      final PayPeriodFrequencyRepository payPeriodFrequencyRepository,
      final PayrollDetailService payrollDetailService) {
    this.payPeriodFrequencyRepository = payPeriodFrequencyRepository;
    this.payrollDetailService = payrollDetailService;
  }

  public List<StaticCompanyPayFrequencyType> findAll() {
    return payPeriodFrequencyRepository.findAll();
  }

  public StaticCompanyPayFrequencyType findByName(final String name) {
    return payPeriodFrequencyRepository.findByName(name);
  }

  public StaticCompanyPayFrequencyType findById(final String id) {
    final Optional<StaticCompanyPayFrequencyType> payFrequencyType =
        payPeriodFrequencyRepository.findById(id);
    return payFrequencyType.orElseThrow(
        () ->
            new ResourceNotFoundException(
                String.format("payFrequencyType with id %s not found!", id),
                id,
                "payFrequencyType"));
  }

  public Optional<StaticCompanyPayFrequencyType> findSetting() {
    Optional<CompanyTaSetting> companySetting = Optional
        .ofNullable(attendanceSettingsService.findCompanySetting());
    return companySetting.map(CompanyTaSetting::getPayFrequencyType);
  }
}
