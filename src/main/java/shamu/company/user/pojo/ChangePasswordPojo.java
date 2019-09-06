package shamu.company.user.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangePasswordPojo {

  private String passWord;

  private String newPassword;
}
