package shamu.company.user.dto;

import com.auth0.json.mgmt.users.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class IndeedUserDto extends User {

  private String firstName;

  private String lastName;

  private String companyName;

}
