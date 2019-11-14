package shamu.company.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BasicUserPersonalInformationDto {

  private String id;

  private String firstName;

  private String middleName;

  private String lastName;

  private String preferredName;

  private String birthDate;
}
