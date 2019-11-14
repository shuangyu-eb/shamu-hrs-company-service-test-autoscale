package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import java.util.Set;
import lombok.Data;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;

@Data
public class TimeOffRequestDto {

  private String id;

  private String imageUrl;

  private String userId;

  private String name;

  private String policyName;

  private Timestamp startDay;

  private Timestamp endDay;

  private Integer hours;

  private Timestamp createdAt;

  private Timestamp approvedDate;

  private TimeOffRequestApprovalStatus status;

  private String comment;

  private Set<TimeOffRequestDateDto> dates;

  private Boolean hasPermissionCreateAndApprove;
}
