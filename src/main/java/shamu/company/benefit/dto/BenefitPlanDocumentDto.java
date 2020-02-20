package shamu.company.benefit.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.config.SerializerUrl;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanDocumentDto {

  private String id;

  private String title;

  @JsonSerialize(using = SerializerUrl.class)
  private String url;

  private String fileName;
}
