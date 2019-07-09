package shamu.company.timeoff.service;

import java.util.List;
import shamu.company.company.entity.Company;
import shamu.company.timeoff.dto.PaidHolidayDto;

public interface PaidHolidayService {

  void initDefaultPaidHolidays(Company company);

  List<PaidHolidayDto>  getPaidHolidays(Long companyId);

  void updateHolidaySelects(List<PaidHolidayDto> paidHolidayDtos);

  PaidHolidayDto createPaidHoliday(PaidHolidayDto paidHolidayDto, Company company);

  void updatePaidHoliday(PaidHolidayDto paidHolidayDto);

  void deletePaidHoliday(Long id);
}
