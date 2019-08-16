package shamu.company.timeoff.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.common.mapper.Config;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;

@Mapper(config = Config.class)
public interface AccrualScheduleMilestoneMapper {

  @Mapping(target = "timeOffPolicyAccrualScheduleId", source = "timeOffPolicyAccrualScheduleId")
  @Mapping(target = "id", ignore = true)
  AccrualScheduleMilestone createFromAccrualScheduleMilestoneDtoAndTimeOffPolicyAccrualScheduleId(
      AccrualScheduleMilestoneDto accrualScheduleMilestoneDto, Long timeOffPolicyAccrualScheduleId);

  @Mapping(target = "timeOffPolicyAccrualScheduleId", source = "timeOffPolicyAccrualScheduleId")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "deletedAt", expression = "java(null)")
  void updateFromAccrualScheduleMilestoneDtoAndTimeOffPolicyAccrualScheduleId(
      @MappingTarget AccrualScheduleMilestone accrualScheduleMilestone,
      AccrualScheduleMilestoneDto accrualScheduleMilestoneDto, Long timeOffPolicyAccrualScheduleId);
}
