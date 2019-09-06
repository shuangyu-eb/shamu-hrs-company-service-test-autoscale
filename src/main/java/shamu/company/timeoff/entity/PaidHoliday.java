package shamu.company.timeoff.entity;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.common.entity.Country;
import shamu.company.company.entity.Company;

@Data
@Entity
@Table(name = "paid_holidays")
@NoArgsConstructor
@AllArgsConstructor
public class PaidHoliday extends BaseEntity {

  @ManyToOne
  private Country country;

  @ManyToOne
  private Company company;

  private String name;

  private String nameShow;

  private Timestamp date;

  private Boolean federal = false;
}
