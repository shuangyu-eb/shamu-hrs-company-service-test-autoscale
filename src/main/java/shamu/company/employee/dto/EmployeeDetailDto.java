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
public class EmployeeDetailDto {

  private String userStatus;

  private Timestamp emailSendDate;

  private String firstName;

  private String lastName;

  private String preferredName;

  private String imageUrl;

  private String workPhone;

  private String workEmail;

  private String jobTitle;

  private String roleName;

  private EmployeeDetailManagerDto manager;

  private List<JobUserDto> directReporters;

  public String getRoleName() {
    if (StringUtils.equals(Role.ADMIN.getValue(), roleName)
        || StringUtils.equals(Role.INACTIVATE.getValue(), roleName)) {
      return roleName;
    }
    return null;
  }
}
