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

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "company_ta_settings")
public class CompanyTaSetting extends BaseEntity {

  private static final long serialVersionUID = -8716988981910506553L;
  @OneToOne private Company company;

  @ManyToOne private StaticTimezone timeZone;

  private int approvalDaysBeforePayroll;

  private String startOfWeek;

  private int messagingOn;

  private int overtimeAlert;
}
