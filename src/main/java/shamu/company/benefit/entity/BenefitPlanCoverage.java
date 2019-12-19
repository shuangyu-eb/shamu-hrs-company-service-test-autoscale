package shamu.company.benefit.entity;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
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
  @Column(name = "benefit_plan_id")
  private Long benefitPlanId;

  private String name;

  private BigDecimal employeeCost;

  private BigDecimal employerCost;
}
