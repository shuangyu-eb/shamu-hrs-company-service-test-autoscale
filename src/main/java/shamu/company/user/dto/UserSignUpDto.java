package shamu.company.user.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSignUpDto {

  private String firstName;

  private String lastName;

  private String companyName;

  private String workEmail;

  private String password;
}
