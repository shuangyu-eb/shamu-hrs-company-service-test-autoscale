package shamu.company.benefit.dto;

import java.sql.Date;
import lombok.Data;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.company.entity.Company;
import shamu.company.s3.PreSinged;

@Data
public class BenefitPlanCreateDto {

  private String planName;

  private String imageUrl;

  private String description;

  private String planId;

  private String planWebSite;

  private String retirementTypeId;

  private String benefitPlanTypeId;

  private String documentName;

  @PreSinged
  private String documentUrl;

  private Date startDate;

  private Date endDate;

  public BenefitPlan getBenefitPlan(final Company company) {
    return new BenefitPlan(this.planName, this.description, this.planId, this.startDate,
        this.endDate,
        this.documentName, this.documentUrl, company, this.planWebSite,
        new BenefitPlanType(this.benefitPlanTypeId));
  }
}
