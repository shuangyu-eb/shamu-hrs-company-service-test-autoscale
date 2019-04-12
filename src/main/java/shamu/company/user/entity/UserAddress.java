package shamu.company.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;

import javax.persistence.*;

@Entity
@Data
@Table(name = "user_addresses")
@Where(clause = "deleted_at IS NULL")
@AllArgsConstructor
@NoArgsConstructor
public class UserAddress extends BaseEntity {
    @OneToOne
    @JsonIgnore
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
