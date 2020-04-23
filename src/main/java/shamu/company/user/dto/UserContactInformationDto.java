package shamu.company.user.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserContactInformationDto extends BasicUserContactInformationDto {

  @Email private String emailHome;

  @Email @NotBlank private String emailWork;
}
