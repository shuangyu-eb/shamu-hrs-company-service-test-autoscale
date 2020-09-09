package shamu.company.attendance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;
import shamu.company.company.entity.Company;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "overtime_policies")
public class OvertimePolicy extends BaseEntity {
  private static final long serialVersionUID = 5425452431570902911L;
  private String policyName;

  @ManyToOne private Company company;

  private Boolean defaultPolicy;

  private Boolean active;
}
