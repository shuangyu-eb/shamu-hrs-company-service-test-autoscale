package shamu.company.user.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.common.mapper.Config;
import shamu.company.employee.dto.CompensationDto;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.user.entity.UserCompensation;

@Mapper(config = Config.class)
public interface UserCompensationMapper {

  @Mapping(target = "id", source = "userCompensationId")
  @Mapping(target = "wage", source = "compensationWage")
  @Mapping(target = "compensationFrequency", source = "compensationFrequencyId")
  void updateFromJobUpdateDto(
      @MappingTarget UserCompensation userCompensation, JobUpdateDto jobUpdateDto);

  CompensationDto convertToCompensationDto(UserCompensation userCompensation);

  default CompensationFrequency convertToCompensationFrequency(final String id) {
    final CompensationFrequency compensationFrequency = new CompensationFrequency();
    compensationFrequency.setId(id);
    return compensationFrequency;
  }
}
