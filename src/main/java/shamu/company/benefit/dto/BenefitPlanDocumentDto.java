package shamu.company.benefit.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import shamu.company.common.config.SerializerUrl;

@Data
public class BenefitPlanDocumentDto {

  private String id;

  private String title;

  @JsonSerialize(using = SerializerUrl.class)
  private String url;

  private String fileName;
}
