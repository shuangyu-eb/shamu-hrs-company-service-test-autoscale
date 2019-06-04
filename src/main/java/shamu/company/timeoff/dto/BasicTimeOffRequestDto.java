package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.user.entity.User;

@Data
public class BasicTimeOffRequestDto {

  @HashidsFormat
  private Long id;

  private String name;

  private Timestamp startDay;

  private Timestamp endDay;

  public BasicTimeOffRequestDto(TimeOffRequest timeOffRequest) {
    this.id = timeOffRequest.getId();
    User requester = timeOffRequest.getRequesterUser();

    this.name = requester.getUserPersonalInformation().getFirstName();
    this.startDay = timeOffRequest.getStartDay();
    this.endDay = timeOffRequest.getEndDay();
  }

}
