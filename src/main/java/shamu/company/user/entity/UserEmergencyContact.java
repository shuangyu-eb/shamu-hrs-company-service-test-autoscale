package shamu.company.user.entity;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "user_emergency_contacts")
public class UserEmergencyContact extends BaseEntity {
    @OneToOne
    private User user;

    private String firstName;

    private String lastName;

    private String relationships;

    private String phone;

    private String email;

    private String street1;

    private String street2;

    private String city;

    private String stateId;

    private String postalCode;

    private Byte isPrimary;

}
