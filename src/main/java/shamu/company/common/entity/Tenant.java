package shamu.company.common.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

@Data
@Table(name = "tenants")
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tenant extends BaseEntity {

  private static final long serialVersionUID = -2712937581770881563L;

  @Length(max = 244)
  private String name;

  private String imageUrl;

  @Column(name = "EIN")
  private String ein;

  @ManyToOne private Country country;

  private Boolean isPaidHolidaysAutoEnroll;

  @Type(type = "shamu.company.common.PrimaryKeyTypeDescriptor")
  private String companyId;
}
