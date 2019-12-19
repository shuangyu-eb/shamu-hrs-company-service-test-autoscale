package shamu.company.benefit.dto;

import java.sql.Timestamp;

import lombok.Data;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.company.entity.Company;

@Data
public class BenefitPlanDto {

  private String id;

  private String name;

  private String description;

  private String planId;

  private Timestamp startDate;

  private Timestamp endDate;

  private String documentName;

  private String documentUrl;

  private String website;

  private Company company;

  private BenefitPlanType benefitPlanType;
}
