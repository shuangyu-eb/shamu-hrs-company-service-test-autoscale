package shamu.company.attendance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.repository.PayPeriodFrequencyRepository;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class PayPeriodFrequencyService {
  private final PayPeriodFrequencyRepository payPeriodFrequencyRepository;
  private final AttendanceSettingsService attendanceSettingsService;

  @Autowired
  public PayPeriodFrequencyService(
      final PayPeriodFrequencyRepository payPeriodFrequencyRepository,
      final AttendanceSettingsService attendanceSettingsService) {
    this.payPeriodFrequencyRepository = payPeriodFrequencyRepository;
    this.attendanceSettingsService = attendanceSettingsService;
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

  public StaticCompanyPayFrequencyType findByCompany(final String companyId) {
    return attendanceSettingsService.findCompanySettings(companyId).getPayFrequencyType();
  }
}
