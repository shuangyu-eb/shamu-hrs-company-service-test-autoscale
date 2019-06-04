package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;
import shamu.company.user.entity.User;

@Data
public class TimeOffRequestDto {

  @HashidsFormat
  private Long id;

  private String imageUrl;

  private Long userId;

  private String name;

  private String type;

  private Timestamp startDay;

  private Timestamp endDay;

  private Integer hours;

  private Timestamp createdAt;

  private TimeOffRequestApprovalStatus status;

  private String comment;

  public TimeOffRequestDto(TimeOffRequest timeOffRequest) {
    this.id = timeOffRequest.getId();
    this.status = timeOffRequest.getTimeOffApprovalStatus();
    this.comment = timeOffRequest.getComment();

    this.createdAt = timeOffRequest.getCreatedAt();
    User requester = timeOffRequest.getRequesterUser();

    this.imageUrl = requester.getImageUrl();
    this.userId = requester.getId();
    this.name = requester.getUserPersonalInformation().getFirstName();

    this.type = timeOffRequest.getTimeOffPolicy().getName();
    this.hours = timeOffRequest.getHours();
    this.startDay = timeOffRequest.getStartDay();

    this.endDay = timeOffRequest.getEndDay();
  }
}
