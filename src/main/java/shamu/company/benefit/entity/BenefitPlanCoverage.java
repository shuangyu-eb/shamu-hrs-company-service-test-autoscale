package shamu.company.benefit.entity;

import java.math.BigDecimal;

import javax.persistence.Entity;
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
  @ManyToOne
  private BenefitPlan benefitPlan;

  private String name;

  private BigDecimal employeeCost;

  private BigDecimal employerCost;
}
