package shamu.company.employee.dto;

import javax.validation.constraints.Email;
import lombok.Data;

@Data
public class EmailUpdateDto {

  @Email
  private String email;

  private String password;
}
