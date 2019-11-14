package shamu.company.job.dto;

import java.sql.Timestamp;
import lombok.Data;

@Data
public class JobUpdateDto {

  private String jobUserId;

  private String userCompensationId;

  private Integer compensationWage;

  private String compensationFrequencyId;

  private String employmentTypeId;

  private String jobId;

  private String managerId;

  private String officeId;

  private Timestamp startDate;
}
