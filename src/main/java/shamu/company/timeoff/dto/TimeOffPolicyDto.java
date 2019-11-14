package shamu.company.timeoff.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimeOffPolicyDto {

  private String id;

  private String name;

  private Boolean isLimited;
}
