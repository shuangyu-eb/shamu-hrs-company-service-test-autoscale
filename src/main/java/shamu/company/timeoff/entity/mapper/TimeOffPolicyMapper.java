package shamu.company.timeoff.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.common.mapper.Config;
import shamu.company.company.entity.Company;
import shamu.company.timeoff.dto.TimeOffPolicyDto;
import shamu.company.timeoff.dto.TimeOffPolicyFrontendDto;
import shamu.company.timeoff.entity.TimeOffPolicy;

@Mapper(config = Config.class)
public interface TimeOffPolicyMapper {

  TimeOffPolicyDto convertToTimeOffPolicyDto(TimeOffPolicy timeOffPolicy);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "name", source = "timeOffPolicyFrontendDto.policyName")
  TimeOffPolicy createFromTimeOffPolicyFrontendDtoAndCompany(
      TimeOffPolicyFrontendDto timeOffPolicyFrontendDto, Company company);

  @Mapping(target = "name", source = "policyName")
  void updateFromTimeOffPolicyFrontendDto(@MappingTarget TimeOffPolicy timeOffPolicy,
      TimeOffPolicyFrontendDto timeOffPolicyFrontendDto);
}
