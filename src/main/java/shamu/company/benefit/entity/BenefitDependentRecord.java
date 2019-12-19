package shamu.company.benefit.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "benefit_plan_dependents")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class BenefitDependentRecord extends BaseEntity {

  @Column(name = "benefit_plans_users_id")
  private String benefitPlansUsersId;

  @Column(name = "user_dependents_id")
  private String userDependentsId;

  public BenefitDependentRecord(String benefitPlansUsersId, String userDependentsId) {
    this.benefitPlansUsersId = benefitPlansUsersId;
    this.userDependentsId = userDependentsId;
  }

}
