package shamu.company.employee.dto;

import java.sql.Timestamp;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewEmployeeJobInformationDto {

  @NotBlank
  private String jobId;

  private String employmentTypeId;

  @NotNull
  private Timestamp hireDate;

  @NotBlank
  private String reportsTo;

  private Integer compensation;

  private String compensationFrequencyId;

  private String officeId;
}
