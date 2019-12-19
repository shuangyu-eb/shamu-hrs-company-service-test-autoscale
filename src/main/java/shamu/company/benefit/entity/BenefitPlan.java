package shamu.company.benefit.entity;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "benefit_plans")
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlan extends BaseEntity {

  private String name;

  private String description;

  private String planId;

  private Timestamp startDate;

  private Timestamp endDate;

  private String documentName;

  private String documentUrl;

  @ManyToOne
  private Company company;

  private String website;

  @OneToOne
  private BenefitPlanType benefitPlanType;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "benefit_plan_id")
  private Set<BenefitPlanCoverage> coverages = new HashSet<>();

  public BenefitPlan(final String id) {
    setId(id);
  }

  public BenefitPlan(String name, String description, String planId, Timestamp startDate,
                     Timestamp endDate, String documentName, String documentUrl,
      Company company, String website, BenefitPlanType benefitPlanType) {
    this.name = name;
    this.description = description;
    this.planId = planId;
    this.startDate = startDate;
    this.endDate = endDate;
    this.documentName = documentName;
    this.documentUrl = documentUrl;
    this.company = company;
    this.website = website;
    this.benefitPlanType = benefitPlanType;
  }
}
