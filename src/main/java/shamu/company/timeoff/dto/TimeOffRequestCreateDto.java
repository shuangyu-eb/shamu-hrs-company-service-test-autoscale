package shamu.company.timeoff.dto;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestComment;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.user.entity.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeOffRequestCreateDto {

  private List<TimeOffRequestDateDto> dates;

  private String comment;

  @HashidsFormat private Long policyId;

  @HashidsFormat private Long policyUser;

  private Integer hours;

  public TimeOffRequest getTimeOffRequest(final User requester) {
    final TimeOffRequest timeOffRequest = new TimeOffRequest();
    timeOffRequest.setRequesterUser(requester);
    if (Strings.isNotBlank(comment)) {
      final TimeOffRequestComment timeOffRequestComment =
          new TimeOffRequestComment(requester, comment);
      timeOffRequest.setComment(timeOffRequestComment);
    }

    return timeOffRequest;
  }

  public List<TimeOffRequestDate> getTimeOffRequestDates(final TimeOffRequest timeOffRequest) {
    return dates.stream()
        .map(
            date -> {
              TimeOffRequestDate timeOffRequestDate = new TimeOffRequestDate();
              timeOffRequestDate.setDate(date.getDate());
              timeOffRequestDate.setHours(date.getHours());
              timeOffRequestDate.setTimeOffRequestId(timeOffRequest.getId());
              return timeOffRequestDate;
            })
        .collect(Collectors.toList());
  }
}
