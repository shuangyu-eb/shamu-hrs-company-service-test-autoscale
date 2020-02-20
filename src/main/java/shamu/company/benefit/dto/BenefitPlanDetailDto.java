package shamu.company.benefit.dto;

import java.util.List;
import lombok.Data;
import shamu.company.benefit.entity.BenefitPlanDocument;

@Data
public class BenefitPlanDetailDto {

  private String id;

  private String name;

  private String description;

  private String website;

  private List<BenefitPlanDocumentDto> documents;
}
