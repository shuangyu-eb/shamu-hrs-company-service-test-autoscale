package shamu.company.benefit.entity;

import java.sql.Date;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "benefit_plans")
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class BenefitPlan extends BaseEntity {
  private String name;

  private String description;

  private String planId;

  private Date startDate;

  private Date endDate;

  private String documentName;

  private String documentUrl;

  @ManyToOne
  private Company company;

  private String website;

  @OneToOne
  private BenefitPlanType benefitPlanType;
}
