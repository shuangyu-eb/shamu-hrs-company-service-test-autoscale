package shamu.company.timeoff.dto;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import shamu.company.employee.dto.SelectFieldInformationDto;

@Data
public class TimeOffRequestDetailDto extends TimeOffRequestDto {

  private String userId;

  private Integer balance;

  private SelectFieldInformationDto approver;

  private List<BasicTimeOffRequestDto> otherTimeOffRequests = new LinkedList<>();

  private List<TimeOffRequestCommentDto> approverComments;

  private Boolean isLimited;

  private Boolean isCurrentUserPrivileged = false;
}
