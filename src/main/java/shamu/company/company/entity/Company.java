package shamu.company.company.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.Country;

import javax.persistence.*;

@Data
@Entity
@Table(name = "companies")
@NoArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class Company extends BaseEntity {

    private String name;

    private String imageUrl;

    private String EIN;

    @OneToOne
    private CompanySize companySize;

    @ManyToOne
    private Country country;

    private String subdomainName;
}
