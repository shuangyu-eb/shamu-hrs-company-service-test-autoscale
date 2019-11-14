package shamu.company.user.dto;

import javax.validation.constraints.Email;
import lombok.Data;

@Data
public class UserContactInformationDto extends BasicUserContactInformationDto {

  @Email
  private String emailHome;

  @Email
  private String emailWork;
}
