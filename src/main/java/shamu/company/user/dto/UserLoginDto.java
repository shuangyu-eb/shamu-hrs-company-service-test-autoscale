package shamu.company.user.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginDto {

  @Email
  @NotBlank
  private String emailWork;

  @NotBlank
  private String password;
}
