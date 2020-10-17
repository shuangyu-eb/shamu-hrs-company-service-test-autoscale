package shamu.company.benefit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import lombok.Data;

@Data
public class BenefitPlanCreateDto implements Serializable {

  private static final long serialVersionUID = -2640569462933796352L;
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

  private Double annualMaximum;

  private Double contributionValue;

  private Double deductionValue;

  private RetirementDto retirement;
}
