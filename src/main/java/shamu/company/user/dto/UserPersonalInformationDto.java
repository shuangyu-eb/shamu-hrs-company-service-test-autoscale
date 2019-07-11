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

  @HashidsFormat private Long genderId;

  private String genderName;

  @HashidsFormat private Long ethnicityId;

  private String ethnicityName;

  private String ssn;

  private String imageUrl;

  public UserPersonalInformationDto(final UserPersonalInformation userPersonalInformation) {
    super(userPersonalInformation);

    final Gender gender = userPersonalInformation.getGender();
    final String sexName = gender == null ? "" : gender.getName();
    final Long sexId = gender == null ? null : gender.getId();
    this.genderId = sexId;
    this.genderName = sexName;

    final Ethnicity ethnicity = userPersonalInformation.getEthnicity();
    final String raceName = ethnicity == null ? "" : ethnicity.getName();
    final Long raceId = ethnicity == null ? null : ethnicity.getId();
    this.ethnicityId = raceId;
    this.ethnicityName = raceName;

    this.ssn = userPersonalInformation.getSsn();
  }

  public UserPersonalInformationDto(
      final UserPersonalInformation userPersonalInformation, final String imageUrl) {
    this(userPersonalInformation);
    this.imageUrl = imageUrl;
  }

  @JSONField(serialize = false)
  public UserPersonalInformation getUserPersonalInformation(final UserPersonalInformation origin) {
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
