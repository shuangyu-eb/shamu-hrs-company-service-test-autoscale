package shamu.company.timeoff.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.entity.PaidHoliday;
import shamu.company.timeoff.pojo.PaidHolidayPojo;
import shamu.company.timeoff.repository.PaidHolidayRepository;
import shamu.company.timeoff.service.PaidHolidayService;

@Service
public class PaidHolidayServiceImpl implements PaidHolidayService {

  private final PaidHolidayRepository paidHolidayRepository;

  private List<String> paidHolidayNames = new ArrayList<>();

  {
    paidHolidayNames.add("New Year\'s Day");
    paidHolidayNames.add("Martin Luther King Jr. Day");
    paidHolidayNames.add("Washington's Birthday");
    paidHolidayNames.add("Memorial Day");
    paidHolidayNames.add("Independence Day");
    paidHolidayNames.add("Columbus Day");
    paidHolidayNames.add("Veterans Day");
    paidHolidayNames.add("Thanksgiving Day");
    paidHolidayNames.add("Christmas Day");
  }

  @Autowired
  public PaidHolidayServiceImpl(PaidHolidayRepository paidHolidayRepository) {
    this.paidHolidayRepository = paidHolidayRepository;
  }

  @Override
  public void createPaidHolidays(Long companyId) {
    List<PaidHoliday> paidHolidays = this.paidHolidayNames.stream().map(paidHolidayName ->
        new PaidHoliday(companyId, paidHolidayName, false, null, false)
    ).collect(Collectors.toList());
    paidHolidayRepository.saveAll(paidHolidays);
  }

  @Override
  public PaidHolidayDto getPaidHolidays(Long companyId) {
    List<PaidHoliday> holidays = paidHolidayRepository.findByCompanyId(companyId);
    // TODO get employeeNum
    return new PaidHolidayDto(holidays, 12);
  }

  @Override
  public void updateHolidaySelects(List<PaidHolidayPojo> holidaySelectValues) {
    holidaySelectValues.forEach(holidaySelectValue ->
        paidHolidayRepository
            .updateHolidaySelect(holidaySelectValue.getId(), holidaySelectValue.getIsSelect())
    );
  }

  @Override
  public void createPaidHoliday(PaidHoliday paidHoliday) {
    paidHolidayRepository.save(paidHoliday);
  }

  @Override
  public void updatePaidHoliday(PaidHoliday paidHoliday) {
    paidHolidayRepository.updateDetail(
        paidHoliday.getId(), paidHoliday.getName(), paidHoliday.getHolidayDate());
  }

  @Override
  public void deletePaidHoliday(Long id) {
    paidHolidayRepository.delete(id);
  }
}
