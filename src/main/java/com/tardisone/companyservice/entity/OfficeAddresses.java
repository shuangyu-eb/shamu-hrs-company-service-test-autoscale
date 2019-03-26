package com.tardisone.companyservice.entity;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "office_addresses")
public class OfficeAddresses {

    @Id
    private  Long id;

    private String street_1;

    private String street_2;

    private String city;

    private String postalCode;

    @ManyToOne
    private Country country;

    @ManyToOne
    @JoinColumn(name="state_province_id")
    private StatesProvince statesProvince;



}
