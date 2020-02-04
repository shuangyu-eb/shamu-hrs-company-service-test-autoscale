package shamu.company.benefit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Timestamp;
import java.util.List;
import lombok.Data;

@Data
public class BenefitPlanCreateDto {

  private String planName;

  private String imageUrl;

  private String description;

  private String planId;

  private String website;

  private String retirementTypeId;

  private String benefitPlanTypeId;

  @JsonFormat(pattern = "MM/dd/yyyy")
  private Timestamp startDate;

  @JsonFormat(pattern = "MM/dd/yyyy")
  private Timestamp endDate;

  private List<String> remainingDocumentIds;
}
