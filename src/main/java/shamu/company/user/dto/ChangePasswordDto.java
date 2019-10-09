package shamu.company.user.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangePasswordDto {

  private String passWord;

  @NotNull
  @Pattern(
      regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$",
      message = "Your password doesn't meet our requirements."
  )
  private String newPassword;
}
