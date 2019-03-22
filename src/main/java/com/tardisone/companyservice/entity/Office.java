package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "offices")
public class Office {

    @Id
    private Long id;

    private Long organization_id;

    private Long office_id;

    private String name;

    private String phone;

    private String email;

    @OneToOne
    private OfficeAddresses address;

    @ManyToOne
    private Company company;

}
