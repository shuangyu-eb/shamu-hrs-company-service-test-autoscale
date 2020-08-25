package shamu.company.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.company.entity.Company;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@Table(name = "payroll_details")
public class PayrollDetail extends BaseEntity {
  private static final long serialVersionUID = -5345409174148025213L;
  @OneToOne private Company company;

  @ManyToOne private StaticCompanyPayFrequencyType payFrequencyType;

  private Timestamp lastPayrollPayday;

  public PayrollDetail(
      final Company company,
      final StaticCompanyPayFrequencyType payFrequencyType,
      final Date lastPayrollPayday) {
    this.company = company;
    this.payFrequencyType = payFrequencyType;
    this.lastPayrollPayday = new Timestamp(lastPayrollPayday.getTime());
  }
}
