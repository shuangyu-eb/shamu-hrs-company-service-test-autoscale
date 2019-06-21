package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "benefit_plans_users")
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class BenefitPlanUser extends BaseEntity {

  @ManyToOne
  private User user;

  @ManyToOne
  private BenefitPlan benefitPlan;


  private Boolean enrolled;

}
