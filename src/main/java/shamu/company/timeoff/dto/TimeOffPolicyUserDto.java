package shamu.company.timeoff.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffPolicyUser;

@Data
public class TimeOffPolicyUserDto {

  @HashidsFormat
  private Long id;

  @HashidsFormat
  private Long userId;

  @HashidsFormat
  private Long timeOffPolicyId;

  private Integer balance;

  @JSONField(serialize = false)
  public TimeOffPolicyUser getTimeOffPolicyUser(Long timeOffPolicyId) {
    return new TimeOffPolicyUser(this.getUserId(), timeOffPolicyId, this.getBalance());
  }
}
