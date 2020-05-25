package shamu.company.attendance.entity;

import javax.persistence.Entity;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
public class Currencies extends BaseEntity {

  private String name;

  private String abbreviation;
}
