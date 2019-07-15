package shamu.company.timeoff.service;

import java.util.List;
import shamu.company.company.entity.Company;
import shamu.company.job.dto.JobUserDto;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.dto.PaidHolidayRelatedUserListDto;

public interface PaidHolidayService {

  void initDefaultPaidHolidays(Company company);

  List<PaidHolidayDto>  getPaidHolidays(Long companyId);

  void updateHolidaySelects(List<PaidHolidayDto> paidHolidayDtos);

  PaidHolidayDto createPaidHoliday(PaidHolidayDto paidHolidayDto, Company company);

  void updatePaidHoliday(PaidHolidayDto paidHolidayDto);

  void deletePaidHoliday(Long id);

  PaidHolidayRelatedUserListDto getPaidHolidayEmployees(Company company);

  void updatePaidHolidayEmployees(List<JobUserDto> newPaidEmployees,Company company);
}
