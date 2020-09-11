package shamu.company.attendance.entity;

import lombok.Data;
import shamu.company.common.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "policy_details")
public class PolicyDetail extends BaseEntity {
  private static final long serialVersionUID = -7274691433537975032L;

  @ManyToOne private OvertimePolicy overtimePolicy;

  private Integer start;

  @JoinColumn(name = "overtime_type_id")
  @ManyToOne
  private StaticOvertimeType staticOvertimeType;

  private BigDecimal rate;
}
