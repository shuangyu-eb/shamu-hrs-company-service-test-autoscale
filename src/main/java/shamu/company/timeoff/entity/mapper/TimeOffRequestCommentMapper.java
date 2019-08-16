package shamu.company.timeoff.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.common.mapper.Config;
import shamu.company.timeoff.dto.TimeOffRequestCommentDto;
import shamu.company.timeoff.entity.TimeOffRequestComment;

@Mapper(config = Config.class)
public interface TimeOffRequestCommentMapper {

  @Mapping(target = "imageUrl", source = "user.imageUrl")
  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "name", source = "user.userPersonalInformation.name")
  TimeOffRequestCommentDto convertToTimeOffRequestCommentDto(
      TimeOffRequestComment timeOffRequestComment);
}
