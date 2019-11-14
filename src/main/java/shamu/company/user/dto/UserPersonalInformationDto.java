package shamu.company.user.dto;

import javax.validation.constraints.Pattern;
import lombok.Data;
import shamu.company.crypto.Crypto;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.UserPersonalInformation;

@Data
public class UserPersonalInformationDto extends BasicUserPersonalInformationDto {

  @HashidsFormat
  private Long genderId;

  private String genderName;

  @HashidsFormat
  private Long ethnicityId;

  private String ethnicityName;

  @Crypto(field = "id", targetType = UserPersonalInformation.class)
  @Pattern(
      regexp =
          "^(\\d{3}-\\d{2}-\\d{4})$/.test(value) "
              + "|| /^(?!00)(?!666)(?!9[0-9][0-9])\\d{3}[- ]?(?!00)\\d{2}[- "
              + "]?(?!0000)\\d{4}$",
      message = "Your ssn doesn't meet our requirements.")
  private String ssn;

  private String imageUrl;

  @HashidsFormat
  private Long maritalStatusId;

  private String maritalStatusName;
}
