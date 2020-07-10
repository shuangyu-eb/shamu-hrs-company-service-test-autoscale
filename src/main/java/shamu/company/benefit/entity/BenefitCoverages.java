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
@Table(name = "benefit_coverages")
@NoArgsConstructor
public class BenefitCoverages extends BaseEntity {

  @Column(name = "benefit_plan_id")
  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String benefitPlanId;

  private String name;

  private int refId;
}
