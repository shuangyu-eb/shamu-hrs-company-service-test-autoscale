package com.tardisone.companyservice.pojo;

import lombok.Data;

@Data
public class OfficeAddressPojo {
    private String officeName;
    private String address;
    private String city;
    private Long state;
    private String zip;
    private Long userId;
}
