package shamu.company.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.validation.constraints.SsnValidate;
import shamu.company.crypto.Crypto;
import shamu.company.crypto.CryptoSsnSerializer;
import shamu.company.user.entity.UserPersonalInformation;

@Data
@NoArgsConstructor
public class UserPersonalInformationDto extends BasicUserPersonalInformationDto {

  private String genderId;

  private String genderName;

  private String ethnicityId;

  private String ethnicityName;

  @Crypto(field = "id", targetType = UserPersonalInformation.class)
  @SsnValidate
  @JsonSerialize(using = CryptoSsnSerializer.class)
  private String ssn;

  private String imageUrl;

  private String maritalStatusId;

  private String maritalStatusName;
}
