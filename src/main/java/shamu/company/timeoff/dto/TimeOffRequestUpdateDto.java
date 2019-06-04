package shamu.company.timeoff.dto;

import lombok.Data;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;

@Data
public class TimeOffRequestUpdateDto {

  private TimeOffRequestApprovalStatus status;

  private String approverComment;

}
