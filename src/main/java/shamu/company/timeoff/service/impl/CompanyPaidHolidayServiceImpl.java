package shamu.company.timeoff.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.entity.CompanyPaidHoliday;
import shamu.company.timeoff.repository.CompanyPaidHolidayRepository;
import shamu.company.timeoff.service.CompanyPaidHolidayService;

@Service
public class CompanyPaidHolidayServiceImpl implements CompanyPaidHolidayService {

  private final CompanyPaidHolidayRepository companyPaidHolidayRepository;

  public CompanyPaidHolidayServiceImpl(
      final CompanyPaidHolidayRepository companyPaidHolidayRepository) {
    this.companyPaidHolidayRepository = companyPaidHolidayRepository;
  }

  @Override
  public CompanyPaidHoliday findCompanyPaidHolidayByPaidHolidayIdAndCompanyId(
      final Long paidHolidayId, final Long companyId) {
    return companyPaidHolidayRepository
        .findCompanyPaidHolidayByPaidHolidayIdAndCompanyId(paidHolidayId, companyId);
  }

  @Override
  public List<CompanyPaidHoliday> findAllByCompanyId(final Long companyId) {
    return companyPaidHolidayRepository.findAllByCompanyId(companyId);
  }


}
