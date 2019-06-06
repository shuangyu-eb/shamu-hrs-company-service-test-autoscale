package shamu.company.timeoff.service;

import java.util.List;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.entity.PaidHoliday;
import shamu.company.timeoff.pojo.PaidHolidayPojo;

public interface PaidHolidayService {

  void createPaidHolidays(Long companyId);

  PaidHolidayDto getPaidHolidays(Long companyId);

  void updateHolidaySelects(List<PaidHolidayPojo> holidaySelectValues);

  void createPaidHoliday(PaidHoliday paidHoliday);

  void updatePaidHoliday(PaidHoliday paidHoliday);

  void deletePaidHoliday(Long id);
}
