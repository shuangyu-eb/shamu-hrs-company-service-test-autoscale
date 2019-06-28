package shamu.company.user.dto;

import com.alibaba.fastjson.annotation.JSONField;
import io.micrometer.core.instrument.util.StringUtils;
import java.sql.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.Ethnicity;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.UserPersonalInformation;

@Data
@NoArgsConstructor
public class UserPersonalInformationDto extends BasicUserPersonalInformationDto {

  @HashidsFormat
  private Long genderId;

  private String genderName;

  @HashidsFormat
  private Long ethnicityId;

  private String ethnicityName;

  private String ssn;

  public UserPersonalInformationDto(UserPersonalInformation userPersonalInformation) {
    super(userPersonalInformation);

    Gender gender = userPersonalInformation.getGender();
    String sexName = gender == null ? "" : gender.getName();
    Long sexId = gender == null ? null : gender.getId();
    this.genderId = sexId;
    this.genderName = sexName;

    Ethnicity ethnicity = userPersonalInformation.getEthnicity();
    String raceName = ethnicity == null ? "" : ethnicity.getName();
    Long raceId = ethnicity == null ? null : ethnicity.getId();
    this.ethnicityId = raceId;
    this.ethnicityName = raceName;

    this.ssn = userPersonalInformation.getSsn();
  }

  @JSONField(serialize = false)
  public UserPersonalInformation getUserPersonalInformation(UserPersonalInformation origin) {
    origin.setFirstName(this.getFirstName());
    origin.setMiddleName(this.getMiddleName());
    origin.setLastName(this.getLastName());
    origin.setPreferredName(this.getPreferredName());
    origin.setSsn(this.getSsn());

    if (this.getBirthDate() != null) {
      origin.setBirthDate(
          StringUtils.isNotBlank(this.getBirthDate()) ? Date.valueOf(this.getBirthDate()) : null);
    }

    if (this.getGenderId() != null) {
      origin.setGender(new Gender(this.getGenderId()));
    }

    if (this.getEthnicityId() != null) {
      origin.setEthnicity(new Ethnicity(this.getEthnicityId()));
    }

    if (this.getMaritalStatusId() != null) {
      origin.setMaritalStatus(new MaritalStatus(this.getMaritalStatusId()));
    }

    return origin;
  }
}
