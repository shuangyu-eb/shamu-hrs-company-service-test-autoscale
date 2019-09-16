package shamu.company.timeoff.pojo;

import javax.persistence.Column;
import javax.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Data;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus.Converter;

@Data
@AllArgsConstructor
public class TimeOffRequestStatusPojo {

  private Long id;

  @Column(name = "time_off_request_approval_status_id")
  @Convert(converter = Converter.class)
  private TimeOffRequestApprovalStatus timeOffApprovalStatus;
}
