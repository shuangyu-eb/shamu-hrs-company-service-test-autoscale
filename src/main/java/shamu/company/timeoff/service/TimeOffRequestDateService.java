package shamu.company.timeoff.service;

import java.util.List;
import shamu.company.timeoff.entity.TimeOffRequestDate;

public interface TimeOffRequestDateService {

  List<TimeOffRequestDate> saveAllTimeOffRequestDates(List<TimeOffRequestDate> dates);
}
