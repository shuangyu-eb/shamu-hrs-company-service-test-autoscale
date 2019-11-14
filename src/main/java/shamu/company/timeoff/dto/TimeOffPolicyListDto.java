package shamu.company.timeoff.dto;

import lombok.Data;

@Data
public class TimeOffPolicyListDto {

  private String id;

  private String name;

  private Integer employee;

  private Boolean isLimited;
}
