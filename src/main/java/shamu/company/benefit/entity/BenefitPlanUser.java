package shamu.company.benefit.entity;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "benefit_plans_users")
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanUser extends BaseEntity {

  @ManyToOne
  private User user;

  @ManyToOne
  private BenefitPlan benefitPlan;

  @ManyToOne
  @JoinColumn(name = "coverage_id")
  private BenefitPlanCoverage benefitPlanCoverage;

  private Boolean enrolled;

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(
      name = "benefit_plan_dependents",
      joinColumns = @JoinColumn(name = "benefit_plans_users_id"),
      inverseJoinColumns = @JoinColumn(name = "user_dependents_id"))
  private Set<BenefitPlanDependent> benefitPlanDependents;

}
