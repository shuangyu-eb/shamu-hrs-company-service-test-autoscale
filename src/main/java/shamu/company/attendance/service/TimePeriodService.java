package shamu.company.attendance.service;

import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheetPeriodPojo;
import shamu.company.attendance.repository.TimePeriodRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TimePeriodService {
  private final TimePeriodRepository timePeriodRepository;

  public TimePeriodService(final TimePeriodRepository timePeriodRepository) {
    this.timePeriodRepository = timePeriodRepository;
  }

  public List<TimeSheetPeriodPojo> listByUser(final String userId) {
    return timePeriodRepository.listTimeSheetPeriodsByUser(userId);
  }

  public List<TimePeriod> listByCompany(final String companyId) {
    return timePeriodRepository.findByCompanyId(companyId);
  }

  public Optional<TimePeriod> findUserCurrentPeriod(final String userId) {
    return Optional.ofNullable(timePeriodRepository.findLatestPeriodByUser(userId));
  }

  public TimePeriod findCompanyCurrentPeriod(final String companyId) {
    return timePeriodRepository.findCompanyNumberNPeriod(companyId, 0);
  }

  public TimePeriod findCompanyLastPeriod(final String companyId) {
    return timePeriodRepository.findCompanyNumberNPeriod(companyId, 1);
  }
}
