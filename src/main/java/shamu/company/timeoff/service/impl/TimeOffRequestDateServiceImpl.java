package shamu.company.timeoff.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.timeoff.entity.TimeOffRequestDate;
import shamu.company.timeoff.repository.TimeOffRequestDateRepository;
import shamu.company.timeoff.service.TimeOffRequestDateService;

@Service
public class TimeOffRequestDateServiceImpl implements TimeOffRequestDateService {

  private final TimeOffRequestDateRepository timeOffRequestDateRepository;

  @Autowired
  public TimeOffRequestDateServiceImpl(TimeOffRequestDateRepository timeOffRequestDateRepository) {
    this.timeOffRequestDateRepository = timeOffRequestDateRepository;
  }

  @Override
  public List<TimeOffRequestDate> saveAllTimeOffRequestDates(List<TimeOffRequestDate> dates) {
    return timeOffRequestDateRepository.saveAll(dates);
  }
}
