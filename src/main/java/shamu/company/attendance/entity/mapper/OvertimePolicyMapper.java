package shamu.company.attendance.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import shamu.company.attendance.dto.OvertimePolicyDto;
import shamu.company.attendance.entity.OvertimePolicy;
import shamu.company.common.mapper.Config;


@Mapper(config = Config.class)
public interface OvertimePolicyMapper {
    @Mapping(target = "company.id", source = "companyId")
    OvertimePolicy convertToOvertimePolicy(
            @MappingTarget OvertimePolicy overtimePolicy,
            OvertimePolicyDto overtimePolicyDto, String companyId);
}