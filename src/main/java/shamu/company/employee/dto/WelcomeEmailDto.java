package shamu.company.employee.dto;

import java.sql.Timestamp;
import lombok.Data;

@Data
public class WelcomeEmailDto {

  private String sendTo;

  private Timestamp sendDate;

  private Boolean rightNow;

  private String personalInformation;
}
