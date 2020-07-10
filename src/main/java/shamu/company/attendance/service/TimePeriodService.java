package shamu.company.attendance.service;

import org.springframework.stereotype.Service;
import shamu.company.attendance.dto.TimePeriodDto;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.repository.TimePeriodRepository;
import shamu.company.utils.ReflectionUtil;

import java.util.List;

@Service
public class TimePeriodService {
  private final TimePeriodRepository timePeriodRepository;

  public TimePeriodService(final TimePeriodRepository timePeriodRepository) {
    this.timePeriodRepository = timePeriodRepository;
  }

  public List<TimePeriodDto> listByUser(final String userId) {
    final List<TimePeriod> timePeriods = timePeriodRepository.listTimePeriodsByUser(userId);
    return ReflectionUtil.convertTo(timePeriods, TimePeriodDto.class);
  }

  public TimePeriod save(final TimePeriod timePeriod) {
    return timePeriodRepository.save(timePeriod);
  }

  public TimePeriod findCompanyCurrentPeriod(final String companyId) {
    return timePeriodRepository.findCompanyNewestPeriod(companyId);
  }
}
