package shamu.company.timeoff.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import shamu.company.company.entity.Company;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffPolicy;

@Data
public class TimeOffPolicyDto {
  @HashidsFormat
  private Long id;

  @HashidsFormat
  private Long companyId;

  private String policyName;

  private Boolean isLimited;

  @JSONField(serialize = false)
  public TimeOffPolicy getTimeOffPolicy(Company company) {
    return new TimeOffPolicy(company, this.getPolicyName(), this.getIsLimited());
  }
}
