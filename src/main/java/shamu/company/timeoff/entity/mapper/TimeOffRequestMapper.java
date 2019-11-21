package shamu.company.timeoff.entity.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.common.mapper.Config;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.timeoff.dto.BasicTimeOffRequestDto;
import shamu.company.timeoff.dto.TimeOffRequestDetailDto;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.dto.TimeOffRequestUpdateDto;
import shamu.company.timeoff.entity.TimeOffRequest;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;

@Mapper(
    config = Config.class,
    uses = {
        TimeOffRequestCommentMapper.class,
        SelectFieldInformationDto.class,
        TimeOffRequestDateMapper.class
    }
)
public interface TimeOffRequestMapper {

  @Mapping(target = "name", source = "requesterUser.userPersonalInformation.name")
  @Mapping(target = "dates", source = "timeOffRequestDates")
  @Mapping(target = "userId", source = "requesterUser.id")
  BasicTimeOffRequestDto convertToBasicTimeOffRequestDto(TimeOffRequest timeOffRequest);

  @Mapping(target = "status", source = "timeOffRequestApprovalStatus.name")
  @Mapping(target = "comment", source = "requsterComment")
  @Mapping(target = "imageUrl", source = "requesterUser.imageUrl")
  @Mapping(target = "userId", source = "requesterUser.id")
  @Mapping(target = "name", source = "requesterUser.userPersonalInformation.name")
  @Mapping(target = "policyName", source = "timeOffPolicy.name")
  @Mapping(target = "dates", source = "timeOffRequestDates")
  TimeOffRequestDto convertToTimeOffRequestDto(TimeOffRequest timeOffRequest);

  @InheritConfiguration
  @Mapping(target = "isLimited", source = "timeOffPolicy.isLimited")
  @Mapping(target = "approver", source = "approverUser")
  @Mapping(target = "approver.name", source = "approverUser.userPersonalInformation.name")
  TimeOffRequestDetailDto convertToTimeOffRequestDetailDto(TimeOffRequest timeOffRequest);

  @Mapping(target = "timeOffRequestApprovalStatus", source = "status")
  TimeOffRequest createFromTimeOffRequestUpdateDto(TimeOffRequestUpdateDto timeOffRequestUpdateDto);

  default TimeOffRequestApprovalStatus convertByTimeOffApprovalStatus(
          TimeOffRequestApprovalStatus.TimeOffApprovalStatus status) {
    TimeOffRequestApprovalStatus timeOffRequestApprovalStatus = new TimeOffRequestApprovalStatus();
    timeOffRequestApprovalStatus.setName(status.name());
    return timeOffRequestApprovalStatus;
  }
}
