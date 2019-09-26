package shamu.company.timeoff.service;

import java.util.List;
import shamu.company.timeoff.entity.CompanyPaidHoliday;

public interface CompanyPaidHolidayService {

  CompanyPaidHoliday findCompanyPaidHolidayByPaidHolidayIdAndCompanyId(Long paidHolidayId,
      Long companyId);

  List<CompanyPaidHoliday> findAllByCompanyId(Long companyId);
}
