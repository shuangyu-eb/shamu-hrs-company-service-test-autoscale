package shamu.company.employee.dto;

import java.sql.Timestamp;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewEmployeeJobInformationDto {

  @NotBlank private String jobId;

  @NotBlank private String departmentId;

  private String employmentTypeId;

  @NotNull private Timestamp hireDate;

  @NotNull private String reportsTo;

  @NotNull private Double compensation;

  @NotNull private String employeeTypeId;

  @NotNull private String payTypeName;

  @NotNull private String compensationFrequencyId;

  @NotNull private String officeId;
}
