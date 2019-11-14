package shamu.company.employee.dto;

import java.sql.Timestamp;
import lombok.Data;

@Data
public class NewEmployeeJobInformationDto {

  private String jobId;

  private String employmentTypeId;

  private Timestamp hireDate;

  private String reportsTo;

  // TODO remove it, we can get it from job
  private String departmentId;

  private Integer compensation;

  private String compensationFrequencyId;

  private String officeId;
}
