package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import javax.validation.constraints.FutureOrPresent;
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

  private String nameShow;

  @FutureOrPresent
  private Timestamp date;

  private Boolean isSelected;

  public PaidHolidayDto(final CompanyPaidHoliday companyPaidHoliday) {
    final PaidHoliday paidHoliday = companyPaidHoliday.getPaidHoliday();
    setId(paidHoliday.getId());
    setName(paidHoliday.getName());
    setNameShow(paidHoliday.getNameShow());
    setDate(paidHoliday.getDate());
    setIsSelected(companyPaidHoliday.getIsSelected());
  }

  public PaidHoliday covertToNewPaidHolidayEntity(final Company company) {
    final PaidHoliday paidHoliday = new PaidHoliday();
    paidHoliday.setCompany(company);
    paidHoliday.setCountry(company.getCountry());
    paidHoliday.setDate(getDate());
    paidHoliday.setName(getName());
    paidHoliday.setNameShow(getNameShow() != null ? getNameShow() : getName());
    return paidHoliday;
  }

  public CompanyPaidHoliday covertToNewCompanyPaidHolidayEntity(final PaidHoliday paidHoliday) {
    final CompanyPaidHoliday companyPaidHoliday = new CompanyPaidHoliday();
    companyPaidHoliday.setIsSelected(getIsSelected());
    companyPaidHoliday.setPaidHoliday(paidHoliday);
    companyPaidHoliday.setCompany(paidHoliday.getCompany());
    return companyPaidHoliday;
  }
}
