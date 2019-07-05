package shamu.company.timeoff.pojo;

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
public class TimeOffRequestPojo {

  private List<TimeOffRequestDatePojo> dates;

  private String comment;

  @HashidsFormat
  private Long policy;

  @HashidsFormat
  private Long policyUser;

  private Integer totalHours;

  public TimeOffRequest getTimeOffRequest(User requester) {
    TimeOffRequest timeOffRequest = new TimeOffRequest();
    timeOffRequest.setRequesterUser(requester);
    timeOffRequest.setApprover(requester.getManagerUser());
    if (Strings.isNotBlank(comment)) {
      TimeOffRequestComment timeOffRequestComment = new TimeOffRequestComment(requester, comment);
      timeOffRequest.setComment(timeOffRequestComment);
    }

    return timeOffRequest;
  }

  public List<TimeOffRequestDate> getTimeOffRequestDates(TimeOffRequest timeOffRequest) {
    return dates.stream().map(date -> {
      TimeOffRequestDate timeOffRequestDate = new TimeOffRequestDate();
      timeOffRequestDate.setDate(date.getDate());
      timeOffRequestDate.setHours(date.getHours());
      timeOffRequestDate.setTimeOffRequestId(timeOffRequest.getId());
      return timeOffRequestDate;
    }).collect(Collectors.toList());
  }
}

