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

    private Long countryId;
}
