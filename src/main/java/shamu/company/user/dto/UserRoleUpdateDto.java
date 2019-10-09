package shamu.company.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.user.entity.User.Role;

@Data
@NoArgsConstructor
public class UserRoleUpdateDto {

  private String passWord;

  private Role userRole;
}
