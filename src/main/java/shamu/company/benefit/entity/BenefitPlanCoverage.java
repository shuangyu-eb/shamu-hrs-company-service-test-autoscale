package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Table(name = "benefit_plan_coverages")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class BenefitPlanCoverage extends BaseEntity {
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  private BenefitPlan benefitPlan;

  private Integer employeeCost;

  private Integer employerCost;
}
