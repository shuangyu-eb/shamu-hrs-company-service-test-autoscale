package com.tardisone.companyservice.dto;

import lombok.Data;

@Data
public class UserAddressDto {
    private Long id;
    private Long userId;
    private String street1;
    private String street2;
    private String cityName;
    private String countryName;
    private Long stateProvinceId;
    private String postalCode;
}
