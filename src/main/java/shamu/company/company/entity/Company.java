package shamu.company.company.entity;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.Country;

@Data
@Entity
@Table(name = "company")
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Company extends BaseEntity {

  private static final long serialVersionUID = -6081798810093033833L;

  @Length(max = 244)
  private String name;

  private String imageUrl;

  @Column(name = "EIN")
  private String ein;

  @ManyToOne private Country country;

  private Boolean isPaidHolidaysAutoEnroll;

  public Company(final String id) {
    setId(id);
  }
}
