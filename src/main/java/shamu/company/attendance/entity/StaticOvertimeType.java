package shamu.company.attendance.entity;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "static_overtime_types")
public class StaticOvertimeType extends BaseEntity {
  private static final long serialVersionUID = -4308417667177950169L;
  private String name;
}
