package shamu.company.benefit.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Timestamp;
import lombok.Data;
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

  @JSONField(format = "MM/dd/yyyy")
  private Timestamp startDate;

  @JSONField(format = "MM/dd/yyyy")
  private Timestamp endDate;
}
