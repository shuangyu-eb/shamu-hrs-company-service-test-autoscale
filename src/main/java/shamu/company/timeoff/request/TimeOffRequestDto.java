package shamu.company.timeoff.request;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Set;
import lombok.Data;
import shamu.company.timeoff.request.entity.TimeOffRequest;
import shamu.company.timeoff.request.entity.TimeOffRequestApprovalStatus;
import shamu.company.timeoff.request.entity.TimeOffRequestDate;
import shamu.company.user.entity.User;

@Data
public class TimeOffRequestDto {

  private Long id;

  private String imageUrl;

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
    this.name = requester.getUserPersonalInformation().getFirstName();

    this.type = timeOffRequest.getTimeOffPolicy().getName();

    if (timeOffRequest.getTimeOffRequestDates() != null) {
      Set<TimeOffRequestDate> timeOffRequestDates = timeOffRequest.getTimeOffRequestDates();
      if (!timeOffRequestDates.isEmpty()) {
        this.hours = timeOffRequestDates.stream().mapToInt(TimeOffRequestDate::getHours).sum();
        this.startDay = timeOffRequestDates.stream()
            .map(TimeOffRequestDate::getDate).max(Comparator.comparingLong(Timestamp::getTime))
            .get();

        this.endDay = timeOffRequestDates.stream()
            .map(TimeOffRequestDate::getDate).min(Comparator.comparingLong(Timestamp::getTime))
            .get();
      }
    }
  }
}
