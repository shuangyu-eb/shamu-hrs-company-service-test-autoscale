package shamu.company.timeoff.dto;

import lombok.Data;

@Data
public class TimeOffPolicyUserFrontendDto implements UserIdDto {

  private String id;

  private String userId;

  private String timeOffPolicyId;

  private Integer balance = 0;
}
