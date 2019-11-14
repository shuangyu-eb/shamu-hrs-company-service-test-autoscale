package shamu.company.timeoff.pojo;

import javax.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.TimeOffApprovalStatus;

@Data
@AllArgsConstructor
public class TimeOffRequestStatusPojo {

  private String id;

  @Column(name = "time_off_request_approval_status_id")
  private TimeOffApprovalStatus timeOffApprovalStatus;

  public TimeOffRequestStatusPojo(String id, TimeOffRequestApprovalStatus approvalStatus) {
    this.id = id;
    this.timeOffApprovalStatus = TimeOffApprovalStatus.valueOf(approvalStatus.getName());
  }
}
