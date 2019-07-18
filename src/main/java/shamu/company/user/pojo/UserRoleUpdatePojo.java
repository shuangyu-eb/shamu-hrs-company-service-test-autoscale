package shamu.company.user.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.user.entity.UserRole.Role;

@Data
@NoArgsConstructor
public class UserRoleUpdatePojo {

  private String passWord;

  private Role userRole;

}
