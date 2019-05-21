package shamu.company.timeoff.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import shamu.company.company.entity.Company;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffType;

@Data
public class TimeOffPolicyDto {
  @HashidsFormat
  private Long id;

  @HashidsFormat
  private Long timeOffTypeId;

  private String policyName;

  private Boolean isLimited;

  @JSONField(serialize = false)
  public TimeOffPolicy getTimeOffPolicy(Company company) {
    TimeOffType timeOffType = new TimeOffType(company.getId(),this.getPolicyName());
    return new TimeOffPolicy(timeOffType,this.getIsLimited());
  }
}
