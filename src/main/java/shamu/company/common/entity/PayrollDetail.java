package shamu.company.common.entity;

import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;

@Data
@Entity
@NoArgsConstructor
@Table(name = "payroll_details")
public class PayrollDetail extends BaseEntity {
  private static final long serialVersionUID = -5345409174148025213L;

  @ManyToOne private StaticCompanyPayFrequencyType payFrequencyType;

  private Timestamp lastPayrollPayday;

  public PayrollDetail(
      final StaticCompanyPayFrequencyType payFrequencyType, final Date lastPayrollPayday) {
    this.payFrequencyType = payFrequencyType;
    this.lastPayrollPayday = new Timestamp(lastPayrollPayday.getTime());
  }
}
