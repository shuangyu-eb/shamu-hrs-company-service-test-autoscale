package shamu.company.employee.dto;

import java.sql.Timestamp;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class NewEmployeeJobInformationDto {

  private String jobTitle;

  @HashidsFormat
  private Long employmentTypeId;

  private Timestamp hireDate;

  @HashidsFormat
  private Long reportsTo;

  @HashidsFormat
  private Long departmentId;

  private Integer compensation;

  @HashidsFormat
  private Long compensationTypeId;

  @HashidsFormat
  private Long officeAddressId;
}
