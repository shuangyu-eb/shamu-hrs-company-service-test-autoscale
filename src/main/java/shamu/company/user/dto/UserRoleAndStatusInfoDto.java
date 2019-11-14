package shamu.company.user.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import lombok.Data;

@Data
public class UserRoleAndStatusInfoDto {

  private String userRole;

  private String userStatus;

  @JSONField(format = "MM/dd/yyyy")
  private Date deactivatedAt;
}
