package shamu.company.timeoff.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "companies_paid_holidays")
@NoArgsConstructor
public class CompanyPaidHoliday extends BaseEntity {

  @ManyToOne
  private Company company;

  @ManyToOne
  private PaidHoliday paidHoliday;

  private Boolean isSelected;

  public CompanyPaidHoliday(PaidHoliday paidHoliday, Company company, Boolean isSelected) {
    setCompany(company);
    setPaidHoliday(paidHoliday);
    setIsSelected(isSelected);
  }
}
