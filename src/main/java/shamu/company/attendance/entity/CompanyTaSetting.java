package shamu.company.attendance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
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
@AllArgsConstructor
@Table(name = "company_ta_settings")
public class CompanyTaSetting extends BaseEntity {

  private static final long serialVersionUID = -8716988981910506553L;
  @OneToOne private Company company;

  @ManyToOne private StaticTimezone timeZone;

  @ManyToOne private StaticCompanyPayFrequencyType payFrequencyType;

  private int approvalDaysBeforePayroll;

  private Timestamp lastPayrollPayday;

  private String startOfWeek;

  private int messagingOn;

  private int overtimeAlert;

  public CompanyTaSetting(
      final Company company,
      final StaticCompanyPayFrequencyType payFrequencyType,
      final Date lastPayrollPayday) {
    this.company = company;
    this.payFrequencyType = payFrequencyType;
    this.lastPayrollPayday = new Timestamp(lastPayrollPayday.getTime());
  }
}
