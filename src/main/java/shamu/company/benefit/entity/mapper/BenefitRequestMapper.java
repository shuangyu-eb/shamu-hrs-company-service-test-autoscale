package shamu.company.benefit.entity.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shamu.company.benefit.dto.BenefitRequestInfoDto;
import shamu.company.benefit.entity.BenefitRequest;
import shamu.company.common.mapper.Config;

@Mapper(config = Config.class)
public interface BenefitRequestMapper {

  List<BenefitRequestInfoDto> convertAllToBenefitRequestInfoDto(
      List<BenefitRequest> benefitRequests);

  @Mapping(target = "name", source = "requestUser.userPersonalInformation.name")
  @Mapping(target = "status", source = "requestStatus.name")
  @Mapping(target = "lifeEventType", source = "lifeEventType.name")
  BenefitRequestInfoDto convertToBenefitRequestInfoDto(BenefitRequest benefitRequest);
}
