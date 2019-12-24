package shamu.company.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Date;
import lombok.Data;

@Data
public class UserRoleAndStatusInfoDto {

  private String userRole;

  private String userStatus;

  @JsonFormat(pattern = "MM/dd/yyyy")
  private Date deactivatedAt;
}
