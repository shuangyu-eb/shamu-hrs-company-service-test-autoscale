package shamu.company.payroll.dto;

import lombok.Data;
import shamu.company.job.entity.JobUserListItem;

@Data
public class PayrollAuthorizedEmployeeDto extends JobUserListItem {
  private String reportsToName;
}
