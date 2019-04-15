package shamu.company.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonalInformationDTO {
    private Long userId;

    private UserAddressDTO userAddress;

    private UserContactInformationDTO userContactInformation;

    private UserPersonalInformationDTO userPersonalInformation;

    public PersonalInformationDTO(Long userId, UserPersonalInformation userPersonalInformation, UserContactInformation userContactInformation, UserAddress userAddress){
        UserPersonalInformationDTO userPersonalInformationDTO = new UserPersonalInformationDTO(userPersonalInformation);
        UserContactInformationDTO userContactInformationDTO = new UserContactInformationDTO(userContactInformation);
        UserAddressDTO userAddressDTO = new UserAddressDTO(userAddress, userId);

        this.setUserAddress(userAddressDTO);
        this.setUserContactInformation(userContactInformationDTO);
        this.setUserPersonalInformation(userPersonalInformationDTO);
        this.setUserId(userId);
    }
}
