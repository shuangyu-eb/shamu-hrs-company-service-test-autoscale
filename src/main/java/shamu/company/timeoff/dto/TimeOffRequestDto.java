package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import java.util.Set;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;

@Data
public class TimeOffRequestDto {

  @HashidsFormat private Long id;

  private String imageUrl;

  @HashidsFormat private Long userId;

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
