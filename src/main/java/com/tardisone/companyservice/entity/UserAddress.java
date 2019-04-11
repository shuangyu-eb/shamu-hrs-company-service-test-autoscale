package com.tardisone.companyservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Data
@Table(name = "user_addresses")
@Where(clause = "deleted_at IS NULL")
@AllArgsConstructor
public class UserAddress extends BaseEntity {
    @OneToOne
    private User user;

    @Column(name = "street_1")
    private String street1;

    @Column(name = "street_2")
    private String street2;

    private String city;

    @ManyToOne
    private StateProvince stateProvince;

    @ManyToOne
    private Country country;

    private String postalCode;
}
