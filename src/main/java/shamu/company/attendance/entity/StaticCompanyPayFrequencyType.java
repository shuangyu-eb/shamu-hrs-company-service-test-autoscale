package shamu.company.attendance.entity;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "static_company_pay_frequency_types")
public class StaticCompanyPayFrequencyType extends BaseEntity {

  private static final long serialVersionUID = 716498755503083616L;
  private String name;

  public enum PayFrequencyType {
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    BIMONTHLY,
  }
}
