package shamu.company.employee.dto;

import java.sql.Timestamp;
import lombok.Data;

@Data
public class NewEmployeeJobInformationDto {

  private String jobId;

  private String employmentTypeId;

  private Timestamp hireDate;

  private String reportsTo;

  private Integer compensation;

  private String compensationFrequencyId;

  private String officeId;
}
