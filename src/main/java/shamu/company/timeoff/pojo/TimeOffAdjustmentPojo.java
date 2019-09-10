package shamu.company.timeoff.pojo;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeOffAdjustmentPojo {

  private Date createdAt;

  private Integer amount;

  private String comment;
}
