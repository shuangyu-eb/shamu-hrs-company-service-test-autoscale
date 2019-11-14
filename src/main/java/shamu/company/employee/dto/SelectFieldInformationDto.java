package shamu.company.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
public class SelectFieldInformationDto {

  private String id;

  private String name;

  public SelectFieldInformationDto(final String id, final String name) {
    setId(id);
    setName(name);
  }

  public SelectFieldInformationDto(final Object object) {
    if (object != null) {
      BeanUtils.copyProperties(object, this);
    }
  }
}
