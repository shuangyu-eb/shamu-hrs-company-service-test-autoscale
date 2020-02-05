package shamu.company.employee.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmployeeDto implements Serializable {

  private String emailWork;

  private String personalPhoto;

  private UserPersonalInformationDto userPersonalInformationDto;

  private UserAddressDto userAddress;

  private UserContactInformationDto userContactInformationDto;

  private List<UserEmergencyContactDto> userEmergencyContactDto;

  private NewEmployeeJobInformationDto jobInformation;

  private WelcomeEmailDto welcomeEmail;
}
