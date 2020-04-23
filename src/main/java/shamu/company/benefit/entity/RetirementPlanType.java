package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "retirement_plans_types")
@NoArgsConstructor
@AllArgsConstructor
public class RetirementPlanType extends BaseEntity {
  @OneToOne private BenefitPlan benefitPlan;

  @OneToOne private RetirementType retirementType;
}
