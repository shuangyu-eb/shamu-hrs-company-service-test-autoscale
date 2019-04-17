package shamu.company.employee.dto;

import lombok.Data;

@Data
public class SelectFieldInformationDto {

  private Long id;

  private String name;

  public SelectFieldInformationDto(Long id, String name) {
    setId(id);
    setName(name);
  }

  public SelectFieldInformationDto(){
  }
}
