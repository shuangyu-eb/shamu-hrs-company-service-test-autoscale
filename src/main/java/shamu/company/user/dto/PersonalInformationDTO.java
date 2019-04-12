package shamu.company.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonalInformationDTO {
    private Long id;

    private UserAddressDTO userAddress;

    private UserContactInformationDTO userContactInformation;

    private UserPersonalInformationDTO userPersonalInformation;
}
