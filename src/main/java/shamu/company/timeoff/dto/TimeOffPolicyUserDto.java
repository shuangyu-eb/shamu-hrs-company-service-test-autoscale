package shamu.company.timeoff.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;

@Data
public class TimeOffPolicyUserDto {

  @HashidsFormat
  private Long id;

  private TimeOffPolicyDto policy;

  private Integer balance;

  public TimeOffPolicyUserDto(TimeOffPolicyUser policyUser) {
    setBalance(policyUser.getBalance());
    setId(policyUser.getId());

    TimeOffPolicyDto dto = new TimeOffPolicyDto();
    TimeOffPolicy thisPolicy = policyUser.getTimeOffPolicy();
    dto.setIsLimited(thisPolicy.getIsLimited());
    dto.setId(thisPolicy.getId());
    dto.setName(thisPolicy.getName());
    setPolicy(dto);
  }
}
