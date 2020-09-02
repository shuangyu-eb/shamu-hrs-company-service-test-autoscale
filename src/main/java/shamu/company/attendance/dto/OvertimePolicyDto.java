package shamu.company.attendance.dto;

import lombok.Data;

import java.util.List;

@Data
public class OvertimePolicyDto {
  private String policyName;

  private Boolean defaultPolicy;

  private List<OvertimePolicyDetailDto> policyDetails;

  private Boolean active;
}
