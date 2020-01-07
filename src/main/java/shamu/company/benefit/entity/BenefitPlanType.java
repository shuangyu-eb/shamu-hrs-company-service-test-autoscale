package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "benefit_plan_types")
@NoArgsConstructor
public class BenefitPlanType extends BaseEntity {

  private static final long serialVersionUID = -7045706432962488490L;
  private String name;

  public BenefitPlanType(final String id) {
    setId(id);
  }

  public enum PlanType {
    MEDICAL("Medical"),
    DENTAL("Dental"),
    VISION("Vision"),
    RETIREMENT("Retirement"),
    OTHER("Other");

    private final String value;

    PlanType(final String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
