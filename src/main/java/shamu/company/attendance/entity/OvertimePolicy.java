package shamu.company.attendance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "overtime_policies")
public class OvertimePolicy extends BaseEntity {
  private static final long serialVersionUID = 5425452431570902911L;

  public static final String NOT_ELIGIBLE_POLICY_NAME = "NOT_ELIGIBLE";

  private String policyName;

  private Boolean defaultPolicy;

  private Boolean active;
}
