package shamu.company.attendance.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheetPeriodPojo;
import shamu.company.attendance.repository.TimePeriodRepository;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;

@Service
public class TimePeriodService {
  private final TimePeriodRepository timePeriodRepository;

  public TimePeriodService(final TimePeriodRepository timePeriodRepository) {
    this.timePeriodRepository = timePeriodRepository;
  }

  public List<TimeSheetPeriodPojo> listByUser(final String userId) {
    return timePeriodRepository.listTimeSheetPeriodsByUser(userId);
  }

  public List<TimePeriod> findAll() {
    return timePeriodRepository.findAll();
  }

  public TimePeriod findById(final String timePeriodId) {
    return timePeriodRepository
        .findById(timePeriodId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Time period with id %s not found!", timePeriodId),
                    timePeriodId,
                    "time period"));
  }

  public Optional<TimePeriod> findUserCurrentPeriod(final String userId) {
    return Optional.ofNullable(timePeriodRepository.findLatestPeriodByUser(userId));
  }

  public TimePeriod findCompanyCurrentPeriod() {
    return timePeriodRepository.findCompanyNumberNPeriod(0);
  }

  public TimePeriod findCompanyLastPeriod() {
    return timePeriodRepository.findCompanyNumberNPeriod(1);
  }

  public TimePeriod save(final TimePeriod timePeriod) {
    return timePeriodRepository.save(timePeriod);
  }
}
