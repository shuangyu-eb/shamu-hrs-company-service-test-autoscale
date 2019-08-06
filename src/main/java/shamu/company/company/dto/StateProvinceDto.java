package shamu.company.company.dto;

import lombok.Data;
import org.springframework.beans.BeanUtils;
import shamu.company.common.entity.StateProvince;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.hashids.HashidsFormat;

@Data
public class StateProvinceDto {

  @HashidsFormat
  private Long id;

  private String name;

  private SelectFieldInformationDto country;

  public StateProvinceDto(final StateProvince stateProvince) {
    if (stateProvince != null) {
      BeanUtils.copyProperties(stateProvince, this);
      setCountry(new SelectFieldInformationDto(stateProvince.getCountry()));
    }
  }
}
