package shamu.company.timeoff.dto;

import java.util.List;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffRequest;

@Data
public class TimeOffRequestDetailDto extends TimeOffRequestDto {

  List<BasicTimeOffRequestDto> otherTimeOffRequests;
  @HashidsFormat
  private Long userId;
  private Integer balance;
  private String approverComment;

  public TimeOffRequestDetailDto(TimeOffRequest timeOffRequest) {
    super(timeOffRequest);
    this.userId = timeOffRequest.getRequesterUser().getId();
    this.approverComment = timeOffRequest.getApproverComment();
  }

}
