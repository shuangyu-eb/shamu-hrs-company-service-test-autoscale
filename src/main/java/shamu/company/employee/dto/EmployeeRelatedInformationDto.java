package shamu.company.employee.dto;

import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import shamu.company.job.dto.JobUserDto;
import shamu.company.user.entity.User.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
// TODO refactor this DTO
public class EmployeeRelatedInformationDto {

  private String userId;

  private String userStatus;

  private Timestamp emailSendDate;

  private String employeeFirstName;

  private String employeeLastName;

  private String employeePreferredName;

  private String employeeImageUrl;

  private String employeeWorkPhone;

  private String employeeWorkEmail;

  private String employeeJobTitle;

  private String managerId;

  private String managerFirstName;

  private String managerLastName;

  private String managerPreferredName;

  private String managerImageUrl;

  private String managerJobTitle;

  private String roleName;

  private List<JobUserDto> directReporters;

  public String getRoleName() {
    if (StringUtils.equals(Role.ADMIN.getValue(), this.roleName)
        || StringUtils.equals(Role.INACTIVATE.getValue(), this.roleName)) {
      return this.roleName;
    }
    return null;
  }
}
