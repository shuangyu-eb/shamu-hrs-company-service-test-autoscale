package shamu.company.timeoff.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.user.entity.User;

@Data
public class TimeOffPolicyUserPojo {

  @HashidsFormat
  private Long id;

  @HashidsFormat
  private Long userId;

  @HashidsFormat
  private Long timeOffPolicyId;

  private Integer balance = 0;

  @JSONField(serialize = false)
  public TimeOffPolicyUser getTimeOffPolicyUser(Long timeOffPolicyId) {
    User user = new User();
    user.setId(this.getUserId());
    TimeOffPolicy policy = new TimeOffPolicy();
    policy.setId(timeOffPolicyId);
    return new TimeOffPolicyUser(user, policy, this.getBalance());
  }
}
