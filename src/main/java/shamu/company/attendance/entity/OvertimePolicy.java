package shamu.company.attendance.entity;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "overtime_policies")
public class OvertimePolicy extends BaseEntity {
  private static final long serialVersionUID = 5425452431570902911L;
  private String policyName;

  @ManyToOne private Company company;

  private Integer universal;

  private Integer defaultPolicy;

  private Integer active;
}
