package shamu.company.job.dto;

import java.sql.Timestamp;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class JobUpdateDto {

  @HashidsFormat
  private Long jobUserId;

  @HashidsFormat
  private Long userCompensationId;

  private Integer compensationWage;

  @HashidsFormat
  private Long compensationFrequencyId;

  @HashidsFormat
  private Long employmentTypeId;

  @HashidsFormat
  private Long jobId;

  @HashidsFormat
  private Long managerId;

  @HashidsFormat
  private Long officeId;

  private Timestamp startDate;
}
