package shamu.company.attendance.entity;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "company_ta_settings")
public class CompanyTaSetting extends BaseEntity {

  @OneToOne private Company company;

  @ManyToOne
  private StaticTimezone staticTimezone;

  @ManyToOne private StaticCompanyPayFrequencyType staticCompanyPayFrequencyType;

  private int approvalDaysBeforePayroll;

  private Timestamp lastPayrollPayday;
}
