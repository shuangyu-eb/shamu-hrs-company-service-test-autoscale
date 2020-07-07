package shamu.company.attendance.dto;

import java.sql.Timestamp;
import lombok.Data;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.company.entity.Company;

@Data
public class CompanyTaSettingsDto {
  private Company company;

  private StaticTimezone timeZone;

  private StaticCompanyPayFrequencyType payFrequencyType;

  private int approvalDaysBeforePayroll;

  private Timestamp lastPayrollPayday;
}
