package shamu.company.attendance.service;

import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import shamu.company.attendance.dto.TimeSheetPeriodDto;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheetPeriodPojo;
import shamu.company.attendance.repository.TimePeriodRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class TimePeriodService {
  private final TimePeriodRepository timePeriodRepository;

  public TimePeriodService(final TimePeriodRepository timePeriodRepository) {
    this.timePeriodRepository = timePeriodRepository;
  }

  public List<TimeSheetPeriodDto> listByUser(final String userId) {
    final List<TimeSheetPeriodPojo> timePeriods =
        timePeriodRepository.listTimeSheetPeriodsByUser(userId);
    final List<TimeSheetPeriodDto> timeSheetPeriodDtoList = new ArrayList<>();
    timePeriods.forEach(
        timePeriod -> {
          final TimeSheetPeriodDto timeSheetPeriodDto = new TimeSheetPeriodDto();
          BeanUtils.copyProperties(timePeriod, timeSheetPeriodDto);
          timeSheetPeriodDtoList.add(timeSheetPeriodDto);
        });
    return timeSheetPeriodDtoList;
  }

  public Optional<TimePeriod> findUserLatestPeriod(final String userId) {
    return Optional.ofNullable(timePeriodRepository.findLatestPeriodByUser(userId));
  }

  public TimePeriod createIfNotExist(final TimePeriod timePeriod) {
    final TimePeriod existedPeriod =
        timePeriodRepository.findByStartDateAndEndDate(
            timePeriod.getStartDate(), timePeriod.getEndDate());
    if (existedPeriod == null) {
      return timePeriodRepository.save(timePeriod);
    }
    return existedPeriod;
  }

  public TimePeriod findCompanyCurrentPeriod(final String companyId) {
    return timePeriodRepository.findCompanyNewestPeriod(companyId);
  }
}
