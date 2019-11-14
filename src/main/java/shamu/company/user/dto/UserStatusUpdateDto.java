package shamu.company.user.dto;

import com.alibaba.fastjson.annotation.JSONField;
import java.sql.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.user.entity.UserStatus.Status;

@Data
@NoArgsConstructor
public class UserStatusUpdateDto {

  private String passWord;

  private Status userStatus;

  private SelectFieldInformationDto deactivationReason;

  @JSONField(format = "MM/dd/yyyy")
  private Date deactivationDate;
}
