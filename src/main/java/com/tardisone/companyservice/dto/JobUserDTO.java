package com.tardisone.companyservice.dto;

import lombok.Data;

@Data
public class JobUserDTO {

    private Long id;

    private String imageUrl;

    private String firstName;

    private String lastName;

    private String email;

    private String jobTitle;

    private String cityName;
}
