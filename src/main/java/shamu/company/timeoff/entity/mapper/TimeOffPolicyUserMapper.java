package shamu.company.timeoff.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.StringUtils;
import shamu.company.common.mapper.Config;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserFrontendDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.user.entity.User;

@Mapper(config = Config.class, uses = TimeOffPolicyMapper.class)
public interface TimeOffPolicyUserMapper {

  @Mapping(target = "policy", source = "timeOffPolicy")
  @Mapping(target = "balance", source = "initialBalance")
  TimeOffPolicyUserDto convertToTimeOffPolicyUserDto(TimeOffPolicyUser timeOffPolicyUser);

  @Mapping(target = "user", source = "timeOffPolicyUserFrontendDto.userId")
  @Mapping(target = "timeOffPolicy", source = "timeOffPolicyId")
  @Mapping(target = "initialBalance", source = "timeOffPolicyUserFrontendDto.balance")
  TimeOffPolicyUser createFromTimeOffPolicyUserFrontendDtoAndTimeOffPolicyId(
      TimeOffPolicyUserFrontendDto timeOffPolicyUserFrontendDto, String timeOffPolicyId);

  default User convertFromUserId(final String userId) {
    return !StringUtils.isEmpty(userId) ? new User(userId) : null;
  }

  default TimeOffPolicy convertFromTimeOffPolicyId(final String timeOffPolicyId) {
    return !StringUtils.isEmpty(timeOffPolicyId) ? new TimeOffPolicy(timeOffPolicyId) : null;
  }
}
