package shamu.company.timeoff.dto;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.user.entity.User;

@Data
public class TimeOffRequestDetailDto extends TimeOffRequestDto {

  @HashidsFormat
  private Long userId;

  private Integer balance;

  private SelectFieldInformationDto approver;

  private List<BasicTimeOffRequestDto> otherTimeOffRequests = new LinkedList<>();

  private List<TimeOffRequestCommentDto> approverComments;

  private Boolean isLimited;

  public TimeOffRequestDetailDto(TimeOffRequest timeOffRequest) {
    super(timeOffRequest);
    this.userId = timeOffRequest.getRequesterUser().getId();
    User approverUser = timeOffRequest.getApproverUser();
    if (approverUser != null) {
      SelectFieldInformationDto aprrover = new SelectFieldInformationDto();
      aprrover.setId(approverUser.getId());
      aprrover.setName(approverUser.getUserPersonalInformation().getName());
      this.approver = aprrover;
    }

    this.approverComments = timeOffRequest.getApproverComments()
        .stream().map(TimeOffRequestCommentDto::new)
        .collect(Collectors.toList());
  }

}
