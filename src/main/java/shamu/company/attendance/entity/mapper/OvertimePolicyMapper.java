package shamu.company.attendance.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import shamu.company.attendance.dto.NewOvertimePolicyDto;
import shamu.company.attendance.dto.OvertimePolicyDetailDto;
import shamu.company.attendance.dto.OvertimePolicyDto;
import shamu.company.attendance.entity.OvertimePolicy;
import shamu.company.common.mapper.Config;

import java.util.List;


@Mapper(config = Config.class)
public interface OvertimePolicyMapper {
    OvertimePolicy convertToOvertimePolicy(
            @MappingTarget OvertimePolicy overtimePolicy,
            NewOvertimePolicyDto newOvertimePolicyDto);

    @Mapping(target="policyDetails", source="policyDetailDtos")
    OvertimePolicyDto convertToOvertimePolicyDto(
            OvertimePolicy overtimePolicy, List<OvertimePolicyDetailDto> policyDetailDtos);
}
