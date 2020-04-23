package shamu.company.employee.dto;

import javax.validation.constraints.Email;
import lombok.Data;

@Data
public class EmailResendDto {

  private String userId;

  @Email private String email;
}
