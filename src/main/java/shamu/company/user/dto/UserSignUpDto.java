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

  private String userId;

  private String firstName;

  private String lastName;

  private String email;

  private String phone;

  private String companyName;

  private String companySize;
}
