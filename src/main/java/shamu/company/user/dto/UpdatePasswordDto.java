package shamu.company.user.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePasswordDto {

  @NotNull
  private String newPassword;

  @NotNull
  private String resetPasswordToken;


}
