package shamu.company.benefit.pojo;

import java.sql.Date;
import lombok.Data;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.company.entity.Company;
import shamu.company.hashids.HashidsFormat;

@Data
public class BenefitPlanPojo {

  private String planName;

  private String imageUrl;

  private String description;

  private String planId;

  private String planWebSite;

  @HashidsFormat
  private Long retirementTypeId;

  @HashidsFormat
  private Long benefitPlanTypeId;

  private String documentName;

  private String documentUrl;

  private Date startDate;

  private Date endDate;

  public BenefitPlan getBenefitPlan(Company company) {
    return new BenefitPlan(this.planName, this.description, this.planId, this.startDate,
        this.endDate,
        this.documentName, this.documentUrl, company, this.planWebSite,
        new BenefitPlanType(this.benefitPlanTypeId));
  }
}
