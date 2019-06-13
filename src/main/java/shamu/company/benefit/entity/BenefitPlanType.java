package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "benefit_plan_types")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class BenefitPlanType extends BaseEntity {

  private String name;

  public BenefitPlanType(Long id) {
    this.setId(id);
  }
}
