package shamu.company.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SelectFieldUserDto extends SelectFieldInformationDto{

  private String userStatus;

  public SelectFieldUserDto(final String id, final String name, final String userStatus) {
    setId(id);
    setName(name);
    setUserStatus(userStatus);
  }
}
