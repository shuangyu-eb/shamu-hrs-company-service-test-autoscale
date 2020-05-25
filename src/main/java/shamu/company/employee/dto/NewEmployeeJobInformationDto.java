package shamu.company.employee.dto;

import java.math.BigInteger;
import java.sql.Timestamp;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewEmployeeJobInformationDto {

  @NotBlank private String jobId;

  private String employmentTypeId;

  @NotNull private Timestamp hireDate;

  private String reportsTo;

  private BigInteger compensation;

  private String compensationFrequencyId;

  private String officeId;
}
