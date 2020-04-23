package shamu.company.benefit.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "benefit_plan_dependents")
@NoArgsConstructor
public class BenefitDependentRecord extends BaseEntity {

  @Column(name = "benefit_plans_users_id")
  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String benefitPlansUsersId;

  @Column(name = "user_dependents_id")
  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String userDependentsId;

  public BenefitDependentRecord(String benefitPlansUsersId, String userDependentsId) {
    this.benefitPlansUsersId = benefitPlansUsersId;
    this.userDependentsId = userDependentsId;
  }
}
