package shamu.company.timeoff.pojo;

import java.sql.Timestamp;

public interface TimeOffRequestDatePojo {

  Timestamp getCreateDate();

  Timestamp getStartDate();

  Timestamp getEndDate();

  Integer getHours();
}
