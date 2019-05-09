package shamu.company.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordDto {
  private String emailWork;

  private String newPassword;

  private String resetPasswordToken;
}
