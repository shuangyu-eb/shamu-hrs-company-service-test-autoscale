package shamu.company.employee.dto;

import javax.validation.constraints.Email;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class EmailResendDto {

  @HashidsFormat
  private Long userId;

  @Email
  private String email;
}
