package shamu.company.user.entity;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "employee_types")
@Data
public class EmployeeType extends BaseEntity {
  private static final long serialVersionUID = -5654004125692433114L;
  private String name;
}
