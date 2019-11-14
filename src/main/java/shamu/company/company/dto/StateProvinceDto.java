package shamu.company.company.dto;

import lombok.Data;
import shamu.company.employee.dto.SelectFieldInformationDto;

@Data
public class StateProvinceDto {

  private String id;

  private String name;

  private SelectFieldInformationDto country;
}
