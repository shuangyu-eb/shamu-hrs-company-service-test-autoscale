package shamu.company.timeoff.service;

import java.util.List;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.entity.CompanyPaidHoliday;
import shamu.company.timeoff.repository.CompanyPaidHolidayRepository;

@Service
public class CompanyPaidHolidayService {

  private final CompanyPaidHolidayRepository companyPaidHolidayRepository;

  public CompanyPaidHolidayService(
      final CompanyPaidHolidayRepository companyPaidHolidayRepository) {
    this.companyPaidHolidayRepository = companyPaidHolidayRepository;
  }

  public CompanyPaidHoliday findCompanyPaidHolidayByPaidHolidayIdAndCompanyId(
      final Long paidHolidayId, final Long companyId) {
    return companyPaidHolidayRepository
        .findCompanyPaidHolidayByPaidHolidayIdAndCompanyId(paidHolidayId, companyId);
  }

  public List<CompanyPaidHoliday> findAllByCompanyId(final Long companyId) {
    return companyPaidHolidayRepository.findAllByCompanyId(companyId);
  }


}