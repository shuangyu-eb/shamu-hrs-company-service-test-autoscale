package shamu.company.company.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "benefits_setting")
@AllArgsConstructor
@NoArgsConstructor
public class CompanyBenefitsSetting extends BaseEntity {

  private static final long serialVersionUID = 1557515580283237947L;

  @Column(name = "automatic_rollover")
  private Boolean isAutomaticRollover;

  @Column(name = "period_start_date")
  @JsonFormat(pattern = "MM/dd/yyyy")
  private Date startDate;

  @Column(name = "period_end_date")
  @JsonFormat(pattern = "MM/dd/yyyy")
  private Date endDate;
}
