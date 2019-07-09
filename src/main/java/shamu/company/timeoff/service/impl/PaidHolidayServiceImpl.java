package shamu.company.timeoff.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.company.entity.Company;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.entity.CompanyPaidHoliday;
import shamu.company.timeoff.entity.PaidHoliday;
import shamu.company.timeoff.repository.CompanyPaidHolidayRepository;
import shamu.company.timeoff.repository.PaidHolidayRepository;
import shamu.company.timeoff.service.PaidHolidayService;

@Service
public class PaidHolidayServiceImpl implements PaidHolidayService {

  private final PaidHolidayRepository paidHolidayRepository;

  private final CompanyPaidHolidayRepository companyPaidHolidayRepository;

  @Autowired
  public PaidHolidayServiceImpl(PaidHolidayRepository paidHolidayRepository,
      CompanyPaidHolidayRepository companyPaidHolidayRepository) {
    this.paidHolidayRepository = paidHolidayRepository;
    this.companyPaidHolidayRepository = companyPaidHolidayRepository;
  }

  @Override
  public void initDefaultPaidHolidays(Company company) {
    //TODO: query by country of company
    List<PaidHoliday> defaultPaidHolidays = paidHolidayRepository.findDefaultPaidHolidays();
    List<CompanyPaidHoliday> companyPaidHolidays = defaultPaidHolidays.stream()
        .map(holiday -> new CompanyPaidHoliday(holiday, company, true))
        .collect(Collectors.toList());
    companyPaidHolidayRepository.saveAll(companyPaidHolidays);
  }

  @Override
  public List<PaidHolidayDto> getPaidHolidays(Long companyId) {
    List<CompanyPaidHoliday> companyPaidHolidays = companyPaidHolidayRepository
        .findAllByCompanyId(companyId);
    return companyPaidHolidays.stream().map(PaidHolidayDto::new)
        .collect(Collectors.toList());
  }

  @Override
  public void updateHolidaySelects(List<PaidHolidayDto> holidaySelectDtos) {
    holidaySelectDtos.forEach(holidaySelectDto ->
        paidHolidayRepository
            .updateHolidaySelect(holidaySelectDto.getId(), holidaySelectDto.getIsSelected())
    );
  }

  @Override
  public PaidHolidayDto createPaidHoliday(PaidHolidayDto paidHolidayDto, Company company) {
    PaidHoliday paidHoliday = paidHolidayDto.covertToNewPaidHolidayEntity(company);
    PaidHoliday paidHolidayReturned = paidHolidayRepository.save(paidHoliday);

    CompanyPaidHoliday companyPaidHoliday = paidHolidayDto
        .covertToNewCompanyPaidHolidayEntity(paidHolidayReturned);
    CompanyPaidHoliday companyPaidHolidayReturned = companyPaidHolidayRepository
        .save(companyPaidHoliday);
    return new PaidHolidayDto(companyPaidHolidayReturned);
  }

  @Override
  public void updatePaidHoliday(PaidHolidayDto paidHolidayDto) {
    paidHolidayRepository.updateDetail(
        paidHolidayDto.getId(), paidHolidayDto.getName(), paidHolidayDto.getDate());
  }

  @Override
  public void deletePaidHoliday(Long id) {
    paidHolidayRepository.delete(id);
  }
}
