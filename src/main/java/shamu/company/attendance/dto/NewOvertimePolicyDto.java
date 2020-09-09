package shamu.company.attendance.dto;

import lombok.Data;

import java.util.List;

@Data
public class NewOvertimePolicyDto {
  private String policyName;

  private Boolean defaultPolicy;

  private List<NewOvertimePolicyDetailDto> policyDetails;

  private Boolean active;
}
