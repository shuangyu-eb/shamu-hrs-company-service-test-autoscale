package shamu.company.user.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.common.mapper.Config;
import shamu.company.employee.dto.CompensationDto;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.user.entity.UserCompensation;

import java.math.BigDecimal;
import java.math.BigInteger;

@Mapper(config = Config.class)
public interface UserCompensationMapper {

  @Mapping(
      target = "wageCents",
      expression = "java(updateCompensationCents(jobUpdateDto.getCompensationWage()))")
  @Mapping(target = "compensationFrequency", source = "compensationFrequencyId")
  void updateFromJobUpdateDto(
      @MappingTarget UserCompensation userCompensation, JobUpdateDto jobUpdateDto);

  @Mapping(target = "wage", expression = "java(updateCompensationDollar(userCompensation))")
  CompensationDto convertToCompensationDto(UserCompensation userCompensation);

  default CompensationFrequency convertToCompensationFrequency(final String id) {
    final CompensationFrequency compensationFrequency = new CompensationFrequency();
    compensationFrequency.setId(id);
    return compensationFrequency;
  }

  default BigInteger updateCompensationCents(final Double compensationWage) {
    final BigDecimal bd = BigDecimal.valueOf(compensationWage * 100);
    return bd.toBigIntegerExact();
  }

  default Double updateCompensationDollar(final UserCompensation userCompensation) {
    return userCompensation.getWageCents().doubleValue() / 100;
  }
}
