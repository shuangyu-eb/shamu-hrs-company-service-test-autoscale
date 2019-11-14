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

  private String name;

  public BenefitPlanType(final String id) {
    setId(id);
  }
}
