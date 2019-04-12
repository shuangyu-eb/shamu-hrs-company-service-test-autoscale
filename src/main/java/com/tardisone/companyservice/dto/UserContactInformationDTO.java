package com.tardisone.companyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContactInformationDTO {
    private Long id;

    private String phoneWork;

    private String phoneHome;

    private String emailWork;

    private String emailHome;
}
