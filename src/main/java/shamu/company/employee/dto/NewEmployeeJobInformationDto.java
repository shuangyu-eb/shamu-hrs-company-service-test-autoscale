package shamu.company.employee.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.sql.Timestamp;

@Data
public class NewEmployeeJobInformationDto {

  @NotBlank private String jobId;

  private String employmentTypeId;

  @NotNull private Timestamp hireDate;

  @NotNull private String reportsTo;

  @NotNull private BigInteger compensation;

  @NotNull private String employeeTypeId;

  @NotNull private String payTypeName;

  @NotNull private String compensationFrequencyId;

  @NotNull private String officeId;
}
