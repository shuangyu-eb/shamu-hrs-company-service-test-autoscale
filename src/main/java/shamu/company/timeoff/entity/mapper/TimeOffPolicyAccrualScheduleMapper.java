package shamu.company.timeoff.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.common.mapper.Config;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;

@Mapper(config = Config.class)
public interface TimeOffPolicyAccrualScheduleMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "timeOffPolicy", source = "timeOffPolicy")
  @Mapping(target = "timeOffAccrualFrequency", source = "timeOffAccrualFrequencyId")
  TimeOffPolicyAccrualSchedule createTimeOffPolicyAccrualSchedule(
      TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto, TimeOffPolicy timeOffPolicy,
      Long timeOffAccrualFrequencyId);

  default TimeOffAccrualFrequency convertTotimeOffAccrualFrequency(final Long id) {
    return new TimeOffAccrualFrequency(id);
  }
}
