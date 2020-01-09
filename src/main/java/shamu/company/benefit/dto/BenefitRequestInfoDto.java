package shamu.company.benefit.dto;

import lombok.Data;

@Data
public class BenefitRequestInfoDto {

  private String name;

  private String imageUrl;

  private String status;

  private String lifeEventType;
}
