package shamu.company.timeoff.entity.mapper;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.common.mapper.Config;
import shamu.company.common.mapper.SelectFieldInformationDtoUtils;
import shamu.company.timeoff.dto.BasicTimeOffRequestDto;
import shamu.company.timeoff.dto.TimeOffRequestDetailDto;
import shamu.company.timeoff.dto.TimeOffRequestDto;
import shamu.company.timeoff.dto.TimeOffRequestUpdateDto;
import shamu.company.timeoff.entity.TimeOffRequest;

@Mapper(
    config = Config.class,
    uses = {
        TimeOffRequestCommentMapper.class,
        SelectFieldInformationDtoUtils.class
    }
)
public interface TimeOffRequestMapper {

  @Mapping(target = "name", source = "requesterUser.userPersonalInformation.firstName")
  BasicTimeOffRequestDto convertToBasicTimeOffRequestDto(TimeOffRequest timeOffRequest);

  @Mapping(target = "status", source = "timeOffApprovalStatus")
  @Mapping(target = "comment", source = "requsterComment")
  @Mapping(target = "imageUrl", source = "requesterUser.imageUrl")
  @Mapping(target = "userId", source = "requesterUser.id")
  @Mapping(target = "name", source = "requesterUser.userPersonalInformation.name")
  @Mapping(target = "policyName", source = "timeOffPolicy.name")
  TimeOffRequestDto convertToTimeOffRequestDto(TimeOffRequest timeOffRequest);

  @InheritConfiguration
  @Mapping(target = "isLimited", source = "timeOffPolicy.isLimited")
  @Mapping(target = "approver", source = "approverUser")
  TimeOffRequestDetailDto convertToTimeOffRequestDetailDto(TimeOffRequest timeOffRequest);

  @Mapping(target = "timeOffApprovalStatus", source = "status")
  TimeOffRequest createFromTimeOffRequestUpdateDto(TimeOffRequestUpdateDto timeOffRequestUpdateDto);
}
