package shamu.company.timeoff.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.timeoff.repository.TimeOffRequestDateRepository;

@Service
public class TimeOffRequestDateService {

  private final TimeOffRequestDateRepository timeOffRequestDateRepository;

  @Autowired
  public TimeOffRequestDateService(
      final TimeOffRequestDateRepository timeOffRequestDateRepository) {
    this.timeOffRequestDateRepository = timeOffRequestDateRepository;
  }

  public List<TimeOffRequestDate> saveAllTimeOffRequestDates(final List<TimeOffRequestDate> dates) {
    return timeOffRequestDateRepository.saveAll(dates);
  }
}
