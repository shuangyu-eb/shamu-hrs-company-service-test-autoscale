package shamu.company.job.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;
import lombok.Data;

@Data
public class JobUpdateDto implements Serializable {
  private static final long serialVersionUID = 3460728440411387457L;

  private String jobUserId;

  private String userCompensationId;

  private BigInteger compensationWage;

  private String compensationFrequencyId;

  private String employmentTypeId;

  private String jobId;

  private String managerId;

  private String officeId;

  private Timestamp startDate;

  private String departmentId;
}
