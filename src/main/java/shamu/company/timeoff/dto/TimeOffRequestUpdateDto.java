package shamu.company.timeoff.dto;

import java.util.Set;
import lombok.Data;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;

@Data
public class TimeOffRequestUpdateDto {

  private TimeOffApprovalStatus status;

  private String approverComment;

  private String policyId;

  private Set<TimeOffRequestDateDto> dates;

}
