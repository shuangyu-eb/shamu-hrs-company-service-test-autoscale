package shamu.company.employee.dto;

import lombok.Data;
import org.springframework.beans.BeanUtils;
import shamu.company.hashids.HashidsFormat;

@Data
public class SelectFieldInformationDto {

  @HashidsFormat
  private Long id;

  private String name;

  public SelectFieldInformationDto(Long id, String name) {
    setId(id);
    setName(name);
  }

  public SelectFieldInformationDto(Object object) {
    if (object != null) {
      BeanUtils.copyProperties(object, this);
    }
  }
}
