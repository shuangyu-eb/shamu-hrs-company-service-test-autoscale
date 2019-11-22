package shamu.company.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.util.StringUtils;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserPersonalInformation extends BaseEntity {

  @NotBlank
  @Length(max = 100)
  private String firstName;

  @Length(max = 100)
  private String middleName;

  @NotBlank
  @Length(max = 100)
  private String lastName;

  @Length(max = 100)
  private String preferredName;

  @JSONField(format = "MM/dd/yyyy")
  private Date birthDate;

  private String ssn;

  @ManyToOne
  private Gender gender;

  @ManyToOne
  private MaritalStatus maritalStatus;

  @ManyToOne
  private Ethnicity ethnicity;

  @ManyToOne
  private CitizenshipStatus citizenshipStatus;

  public String getName() {
    return StringUtils.isEmpty(preferredName)
            ? firstName.concat(" ").concat(lastName)
            : preferredName.concat(" ").concat(lastName);
  }
}
