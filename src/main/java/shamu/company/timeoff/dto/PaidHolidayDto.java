package shamu.company.timeoff.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.company.entity.Company;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.CompanyPaidHoliday;
import shamu.company.timeoff.entity.PaidHoliday;

@Data
@NoArgsConstructor
public class PaidHolidayDto {

  @HashidsFormat
  private Long id;

  private String name;

  private String date;

  private Boolean isSelected;

  public PaidHolidayDto(CompanyPaidHoliday companyPaidHoliday) {
    PaidHoliday paidHoliday = companyPaidHoliday.getPaidHoliday();
    setId(paidHoliday.getId());
    setName(paidHoliday.getName());
    setDate(paidHoliday.getDate());
    setIsSelected(companyPaidHoliday.getIsSelected());
  }

  public PaidHoliday covertToNewPaidHolidayEntity(Company company) {
    PaidHoliday paidHoliday = new PaidHoliday();
    paidHoliday.setCompany(company);
    paidHoliday.setCountry(company.getCountry());
    paidHoliday.setDate(getDate());
    paidHoliday.setName(getName());
    return paidHoliday;
  }

  public CompanyPaidHoliday covertToNewCompanyPaidHolidayEntity(PaidHoliday paidHoliday) {
    CompanyPaidHoliday companyPaidHoliday = new CompanyPaidHoliday();
    companyPaidHoliday.setIsSelected(getIsSelected());
    companyPaidHoliday.setPaidHoliday(paidHoliday);
    companyPaidHoliday.setCompany(paidHoliday.getCompany());
    return companyPaidHoliday;
  }
}
