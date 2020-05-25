package shamu.company.employee.dto;

import java.math.BigInteger;
import lombok.Data;

@Data
public class CompensationDto {

  private String id;

  private BigInteger wage;

  private SelectFieldInformationDto compensationFrequency;
}
