package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "states_provinces")
@Data
public class StatesProvince {

    @Id
    private Long id;

    private String name;

    @OneToOne
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    private Country country;
}
