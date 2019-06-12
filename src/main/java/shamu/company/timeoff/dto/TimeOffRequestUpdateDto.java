package shamu.company.timeoff.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;

@Data
public class TimeOffRequestUpdateDto {

  private TimeOffRequestApprovalStatus status;

  private String approverComment;

  @JSONField(serialize = false)
  public TimeOffRequest getTimeOffRequest() {
    TimeOffRequest timeOffRequest = new TimeOffRequest();
    timeOffRequest.setApproverComment(approverComment);
    timeOffRequest.setTimeOffApprovalStatus(status);
    return timeOffRequest;
  }
}
