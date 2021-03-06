package shamu.company.timeoff.entity.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.common.mapper.Config;
import shamu.company.timeoff.dto.TimeOffPolicyDto;
import shamu.company.timeoff.dto.TimeOffPolicyFrontendDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedInfoDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;

@Mapper(config = Config.class)
public interface TimeOffPolicyMapper {

  TimeOffPolicyDto convertToTimeOffPolicyDto(TimeOffPolicy timeOffPolicy);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "name", source = "timeOffPolicyFrontendDto.policyName")
  @Mapping(target = "isAutoEnrollEnabled", source = "timeOffPolicyFrontendDto.isAutoEnrolled")
  TimeOffPolicy createFromTimeOffPolicyFrontendDto(
      TimeOffPolicyFrontendDto timeOffPolicyFrontendDto);

  @Mapping(target = "name", source = "policyName")
  @Mapping(target = "isAutoEnrollEnabled", source = "isAutoEnrolled")
  void updateFromTimeOffPolicyFrontendDto(
      @MappingTarget TimeOffPolicy timeOffPolicy,
      TimeOffPolicyFrontendDto timeOffPolicyFrontendDto);

  @Mapping(target = "id", source = "timeOffPolicy.id")
  @Mapping(target = "policyName", source = "timeOffPolicy.name")
  @Mapping(target = "isLimited", source = "timeOffPolicy.isLimited")
  @Mapping(target = "isAutoEnrolled", source = "timeOffPolicy.isAutoEnrollEnabled")
  @Mapping(target = "isSickLeavePolicy",source = "timeOffPolicy.isSickLeavePolicy")
  @Mapping(target = "startDate", source = "timeOffPolicyAccrualSchedule.daysBeforeAccrualStarts")
  @Mapping(
      target = "timeOffAccrualFrequency",
      source = "timeOffPolicyAccrualSchedule.timeOffAccrualFrequency.id")
  @Mapping(
      target = "timeOffAccrualFrequencyType",
      source = "timeOffPolicyAccrualSchedule.timeOffAccrualFrequency.name")
  @Mapping(target = "accrualHoursBaseRate", source = "timeOffPolicyAccrualSchedule.accrualHours")
  @Mapping(
      target = "carryoverLimitBaseRate",
      source = "timeOffPolicyAccrualSchedule.carryoverLimit")
  @Mapping(target = "maxBalanceBaseRate", source = "timeOffPolicyAccrualSchedule.maxBalance")
  @Mapping(target = "accrualScheduleMilestone", source = "accrualScheduleMilestones")
  TimeOffPolicyRelatedInfoDto
      createFromTimeOffPolicyAndTimeOffPolicyAccrualScheduleAndAccrualScheduleMilestones(
          TimeOffPolicy timeOffPolicy,
          TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule,
          List<AccrualScheduleMilestone> accrualScheduleMilestones);
}
