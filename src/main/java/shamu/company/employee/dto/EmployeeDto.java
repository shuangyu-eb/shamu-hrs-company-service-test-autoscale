package shamu.company.employee.dto;

import java.util.List;
import lombok.Data;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;

@Data
public class EmployeeDto {

  private String emailWork;

  private String personalPhoto;

  private UserPersonalInformationDto userPersonalInformationDto;

  private UserAddressDto userAddress;

  private UserContactInformationDto userContactInformationDto;

  private List<UserEmergencyContactDto> userEmergencyContactDto;

  private NewEmployeeJobInformationDto jobInformation;

  private WelcomeEmailDto welcomeEmail;
}
