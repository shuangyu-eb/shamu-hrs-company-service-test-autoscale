package shamu.company.timeoff.service;

import java.util.List;
import shamu.company.company.entity.Company;
import shamu.company.job.dto.JobUserDto;
import shamu.company.server.AuthUser;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.dto.PaidHolidayRelatedUserListDto;
import shamu.company.timeoff.entity.PaidHoliday;

public interface PaidHolidayService {

  void initDefaultPaidHolidays(Company company);

  List<PaidHolidayDto> getPaidHolidays(AuthUser user);

  List<PaidHolidayDto> getPaidHolidaysByYear(AuthUser user, String year);

  void updateHolidaySelects(List<PaidHolidayDto> paidHolidayDtos);

  void createPaidHoliday(PaidHolidayDto paidHolidayDto, AuthUser user);

  void updatePaidHoliday(PaidHolidayDto paidHolidayDto);

  void deletePaidHoliday(Long id);

  PaidHolidayRelatedUserListDto getPaidHolidayEmployees(Long companyId);

  void updatePaidHolidayEmployees(List<JobUserDto> newPaidEmployees,Long companyId);

  PaidHoliday getPaidHoliday(Long id);
}
