package shamu.company.employee.dto;

import java.sql.Timestamp;
import lombok.Data;
import shamu.company.user.entity.CompensationType;
import shamu.company.user.entity.UserCompensation;

@Data
public class UserCompensationDto {

  private Integer wage;

  private Timestamp startDate;

  private String overtimeStatus;

  private CompensationType compensationType;

  private String comment;

  public UserCompensationDto(UserCompensation userCompensation) {
    this.wage = userCompensation.getWage();
    this.comment = userCompensation.getComment();
    this.compensationType = userCompensation.getCompensationType();
    this.overtimeStatus = userCompensation.getOvertimeStatus();
    this.startDate = userCompensation.getStartDate();
  }
}
