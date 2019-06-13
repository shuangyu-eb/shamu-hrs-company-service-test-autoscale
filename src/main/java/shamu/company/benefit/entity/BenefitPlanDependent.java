package shamu.company.benefit.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;

@Data
@Entity
@Table(name = "benefit_plan_dependents")
@Where(clause = "deleted_at IS NULL")
public class BenefitPlanDependent extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  private User employee;

  @OneToOne
  private DependentRelationship dependentRelationship;

  private String firstName;

  private String lastName;

  private String phoneHome;

  private String phoneWork;

  private String phoneMobile;

  private String email;
}
