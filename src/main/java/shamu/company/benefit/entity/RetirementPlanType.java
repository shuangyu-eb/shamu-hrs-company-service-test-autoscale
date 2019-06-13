package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "retirement_plans_types")
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class RetirementPlanType extends BaseEntity {
  @OneToOne
  private BenefitPlan benefitPlan;

  @OneToOne
  private RetirementType retirementType;
}
