package shamu.company.timeoff.dto;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.hashids.HashidsFormat;

@Data
public class TimeOffRequestDetailDto extends TimeOffRequestDto {

  @HashidsFormat
  private Long userId;

  private Integer balance;

  private SelectFieldInformationDto approver;

  private List<BasicTimeOffRequestDto> otherTimeOffRequests = new LinkedList<>();

  private List<TimeOffRequestCommentDto> approverComments;

  private Boolean isLimited;
}
