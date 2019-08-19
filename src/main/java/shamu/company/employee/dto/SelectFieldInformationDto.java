package shamu.company.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import shamu.company.hashids.HashidsFormat;

@Data
@NoArgsConstructor
public class SelectFieldInformationDto {

  @HashidsFormat
  private Long id;

  private String name;

  public SelectFieldInformationDto(final Long id, final String name) {
    setId(id);
    setName(name);
  }

  public SelectFieldInformationDto(final Object object) {
    if (object != null) {
      BeanUtils.copyProperties(object, this);
    }
  }
}
