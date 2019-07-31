package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.user.entity.User;

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

  public TimeOffRequestDto(TimeOffRequest timeOffRequest) {
    this.id = timeOffRequest.getId();
    this.status = timeOffRequest.getTimeOffApprovalStatus();
    this.comment = timeOffRequest.getRequsterComment();
    this.comment = timeOffRequest.getRequsterComment();

    this.createdAt = timeOffRequest.getCreatedAt();
    this.approvedDate = timeOffRequest.getApprovedDate();
    User requester = timeOffRequest.getRequesterUser();

    this.imageUrl = requester.getImageUrl();
    this.userId = requester.getId();
    this.name = requester.getUserPersonalInformation().getName();

    this.policyName = timeOffRequest.getTimeOffPolicy().getName();
    this.hours = timeOffRequest.getHours();
    this.startDay = timeOffRequest.getStartDay();

    this.endDay = timeOffRequest.getEndDay();
  }
}
