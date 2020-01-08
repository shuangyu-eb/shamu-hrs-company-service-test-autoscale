package shamu.company.company.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Date;
import lombok.Data;

@Data
public class CompanyBenefitsSettingDto {

  private Boolean isAutomaticRollover;

  @JsonFormat(pattern = "MM/dd/yyyy")
  private Date startDate;

  @JsonFormat(pattern = "MM/dd/yyyy")
  private Date endDate;
}
