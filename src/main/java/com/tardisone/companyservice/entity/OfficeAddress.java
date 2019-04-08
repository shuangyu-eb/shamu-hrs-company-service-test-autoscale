package com.tardisone.companyservice.entity;

import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Data
@Entity
@Table(name = "office_addresses")
@Where(clause = "deleted_at IS NULL")
public class OfficeAddress extends BaseEntity {

    @OneToOne
    private Office office;

    @Column(name = "street_1")
    private String street1;

    @Column(name = "street_2")
    private String street2;

    @ManyToOne
    private City city;

    @ManyToOne
    private StateProvince stateProvince;

    @ManyToOne
    private Country country;

    private String postalCode;

}
