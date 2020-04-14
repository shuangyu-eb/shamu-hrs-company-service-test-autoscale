package shamu.company.timeoff.pojo;

import java.sql.Timestamp;

public interface TimeOffRequestDatePojo {

  String getId();

  Timestamp getCreateDate();

  Integer getHours();
}
