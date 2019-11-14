package shamu.company.user.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import shamu.company.common.entity.BaseEntity;

@Entity
@Data
@Table(name = "compensation_overtime_statuses")
public class CompensationOvertimeStatus extends BaseEntity {

  private String name;
}
