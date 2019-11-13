package shamu.company.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import shamu.company.hashids.HashidsFormat;

@Data
@NoArgsConstructor
public class SelectFieldSizeDto extends SelectFieldInformationDto {

  private Integer size;

  public SelectFieldSizeDto(final Integer size) {
    setSize(size);
  }

  public SelectFieldSizeDto(final Object object) {
    if (object != null) {
      BeanUtils.copyProperties(object, this);
    }
  }
}
