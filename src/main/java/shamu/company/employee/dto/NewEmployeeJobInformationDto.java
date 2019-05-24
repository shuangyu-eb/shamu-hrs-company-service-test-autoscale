package shamu.company.employee.dto;

import java.sql.Timestamp;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class NewEmployeeJobInformationDto {

  @HashidsFormat
  private Long jobId;

  @HashidsFormat private Long employmentTypeId;

  private Timestamp hireDate;

  @HashidsFormat private Long reportsTo;

  @HashidsFormat private Long departmentId;

  private Integer compensation;

  @HashidsFormat
  private Long compensationFrequencyId;

  @HashidsFormat
  private Long officeId;
}
