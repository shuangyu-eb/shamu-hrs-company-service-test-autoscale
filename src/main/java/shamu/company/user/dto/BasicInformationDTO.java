package shamu.company.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasicInformationDTO {

    private UserAddressDTO userAddress;

    private UserPersonalInformationDTO userPersonalInformation;
}
