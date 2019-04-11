package com.tardisone.companyservice.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "user_emergency_contact_relationships")
public class UserEmergencyContactRelationship extends BaseEntity{
    private String name;
}
