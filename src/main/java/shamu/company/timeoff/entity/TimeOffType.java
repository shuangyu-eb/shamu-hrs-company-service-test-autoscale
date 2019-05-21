package shamu.company.timeoff.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.hashids.HashidsFormat;

@Data
@Entity
@Table(name = "time_off_types")
@AllArgsConstructor
@NoArgsConstructor
public class TimeOffType extends BaseEntity {

  @HashidsFormat
  private Long companyId;

  private String name;
}
