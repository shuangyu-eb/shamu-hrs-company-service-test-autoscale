package shamu.company.benefit.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "benefit_plan_documents")
public class BenefitPlanDocument extends BaseEntity {

  private static final long serialVersionUID = -7495808080499740166L;
  private String title;

  private String url;

  @Column(name = "benefit_plan_id")
  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String benefitPlanId;

  public BenefitPlanDocument(final String title, final String url) {
    this.title = title;
    this.url = url;
  }
}
