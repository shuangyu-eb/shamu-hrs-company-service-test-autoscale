package shamu.company.benefit.dto;

import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.company.entity.Company;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanDto {

  private String id;

  private String name;

  private String description;

  private String planId;

  private Timestamp startDate;

  private Timestamp endDate;

  private String website;

  private Company company;

  private BenefitPlanType benefitPlanType;

  private List<BenefitPlanDocumentDto> benefitPlanDocuments;
}
