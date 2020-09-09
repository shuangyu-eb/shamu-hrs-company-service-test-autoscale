package shamu.company.attendance.entity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.attendance.dto.NewOvertimePolicyDetailDto;
import shamu.company.attendance.dto.OvertimePolicyDetailDto;
import shamu.company.attendance.entity.OvertimePolicy;
import shamu.company.attendance.entity.PolicyDetail;
import shamu.company.attendance.entity.StaticOvertimeType;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface PolicyDetailMapper {
  @Mapping(target = "start", source = "overtimePolicyDto.startMin")
  @Mapping(target = "staticOvertimeType", source = "overtimePolicyDto.overtimeType")
  @Mapping(target = "rate", source = "overtimePolicyDto.overtimeRate")
  @Mapping(target = "overtimePolicy", source = "overtimePolicy")
  PolicyDetail convertToPolicyDetail(
          NewOvertimePolicyDetailDto overtimePolicyDto, OvertimePolicy overtimePolicy);

  @Mapping(target = "startMin", source="policyDetail.start")
  @Mapping(target = "overtimeType", source="policyDetail.staticOvertimeType")
  @Mapping(target = "overtimeRate", source="policyDetail.rate")
  OvertimePolicyDetailDto convertToOvertimePolicyDetailDto(
          PolicyDetail policyDetail);

  default StaticOvertimeType.OvertimeType covertByStaticOvertimeType(StaticOvertimeType staticOvertimeType){
    return StaticOvertimeType.OvertimeType.valueOf(staticOvertimeType.getName());
  }

  default StaticOvertimeType convertByOvertimeType(final StaticOvertimeType.OvertimeType otType) {
    final StaticOvertimeType staticOvertimeType = new StaticOvertimeType();
    staticOvertimeType.setName(otType.name());
    return staticOvertimeType;
  }
}
