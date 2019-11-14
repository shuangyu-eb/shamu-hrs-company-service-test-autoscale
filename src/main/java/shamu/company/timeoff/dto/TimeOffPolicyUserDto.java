package shamu.company.timeoff.dto;

import lombok.Data;

@Data
public class TimeOffPolicyUserDto {

  private String id;

  private TimeOffPolicyDto policy;

  private Integer balance;
}
