package shamu.company.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserDto {

  private Long id;

  private String firstName;

  private String lastName;
}
