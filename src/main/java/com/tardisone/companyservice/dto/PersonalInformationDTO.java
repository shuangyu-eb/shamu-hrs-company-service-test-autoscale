package com.tardisone.companyservice.dto;

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
