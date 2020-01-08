package shamu.company.company.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.common.entity.BaseEntity;

@Data
@Entity
@Table(name = "company_benefits_setting")
@AllArgsConstructor
@NoArgsConstructor
public class CompanyBenefitsSetting extends BaseEntity {

  @OneToOne
  private Company company;

  @Column(name = "automatic_rollover")
  private Boolean isAutomaticRollover;

  @Column(name = "period_start_date")
  @JsonFormat(pattern = "MM/dd/yyyy")
  private Date startDate;

  @Column(name = "period_end_date")
  @JsonFormat(pattern = "MM/dd/yyyy")
  private Date endDate;
}
