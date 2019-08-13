package shamu.company.company.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
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
}
