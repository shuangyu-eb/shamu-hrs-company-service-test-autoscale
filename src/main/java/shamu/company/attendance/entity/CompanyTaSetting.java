package shamu.company.attendance.entity;

import java.sql.Timestamp;
import javax.persistence.Entity;
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

  @OneToOne private StaticTimezone staticTimezone;

  @OneToOne private StaticCompanyPayFrequencyType staticCompanyPayFrequencyType;

  private int approvalDaysBeforePayroll;

  private Timestamp lastPayrollPayday;
}
