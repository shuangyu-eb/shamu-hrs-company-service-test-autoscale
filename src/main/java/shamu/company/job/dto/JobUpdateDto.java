package shamu.company.job.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import lombok.Data;

@Data
public class JobUpdateDto implements Serializable {
  private static final long serialVersionUID = 3460728440411387457L;

  private String jobUserId;

  private String userCompensationId;

  private Double compensationWage;

  private String compensationFrequencyId;

  private String employmentTypeId;

  private String jobId;

  private String managerId;

  private String officeId;

  private Timestamp startDate;
}
