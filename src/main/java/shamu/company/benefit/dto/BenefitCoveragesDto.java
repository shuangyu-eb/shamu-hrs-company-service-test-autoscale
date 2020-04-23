package shamu.company.benefit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BenefitCoveragesDto {

  private String id;

  private String coverageName;
}
