package shamu.company.attendance.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "currencies")
public class Currency extends BaseEntity {

  private String name;

  private String abbreviation;
}
