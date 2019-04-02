package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "user_addresses")
@Data
public class UserAddress {

    @Id
    private Long id;

    private Long userId;

    private String street_1;

    private String street_2;

    @OneToOne
    @JoinColumn(name = "city_id", referencedColumnName = "id")
    private City city;

    private Long stateProvinceId;

    @OneToOne
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    private Country country;

    private String postalCode;
}
