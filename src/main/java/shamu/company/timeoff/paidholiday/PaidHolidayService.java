package shamu.company.timeoff.paidholiday;

import java.util.List;

public interface PaidHolidayService {
  void createPaidHolidays(Long companyId);

  PaidHolidayDto getPaidHolidays(Long companyId);

  void updateHolidaySelects(List<PaidHolidayPojo> holidaySelectValues);

  void createPaidHoliday(PaidHoliday paidHoliday);

  void updatePaidHoliday(PaidHoliday paidHoliday);

  void deletePaidHoliday(Long id);
}
