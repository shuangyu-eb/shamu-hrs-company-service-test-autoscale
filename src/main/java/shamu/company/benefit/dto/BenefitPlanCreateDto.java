package shamu.company.benefit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.sql.Timestamp;
import lombok.Data;
import shamu.company.common.config.SerializerUrl;

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

  @JsonSerialize(using = SerializerUrl.class)
  private String documentUrl;

  @JsonFormat(pattern = "MM/dd/yyyy")
  private Timestamp startDate;

  @JsonFormat(pattern = "MM/dd/yyyy")
  private Timestamp endDate;
}
