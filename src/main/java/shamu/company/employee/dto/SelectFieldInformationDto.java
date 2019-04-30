package shamu.company.employee.dto;

import lombok.Data;
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
}
