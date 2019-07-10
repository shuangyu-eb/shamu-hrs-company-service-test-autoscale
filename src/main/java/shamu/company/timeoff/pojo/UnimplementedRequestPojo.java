package shamu.company.timeoff.pojo;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;

@Data
public class UnimplementedRequestPojo {

  @HashidsFormat private Long userId;

  @HashidsFormat private Long requestId;

  private Integer hours;

  private TimeOffRequestApprovalStatus status;
}
