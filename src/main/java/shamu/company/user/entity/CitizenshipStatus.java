package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "citizenship_statuses")
public class CitizenshipStatus extends BaseEntity {

  private String name;
}
