package shamu.company.employee.dto;

import java.sql.Timestamp;
import lombok.Data;

@Data
public class NewEmployeeJobInformationDto {

  private String jobTitle;

  private Long employmentTypeId;

  private Timestamp hireDate;

  private Long reportsTo;

  private Long departmentId;

  private Integer compensation;

  private Long compensationTypeId;

  private Long officeAddressId;
}
