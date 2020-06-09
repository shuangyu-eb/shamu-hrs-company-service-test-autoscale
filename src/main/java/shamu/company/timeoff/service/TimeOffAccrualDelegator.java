package shamu.company.timeoff.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.dto.TimeOffBreakdownYearDto;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.exception.NotFoundException;
import shamu.company.timeoff.pojo.TimeOffBreakdownCalculatePojo;
import shamu.company.timeoff.repository.TimeOffAccrualFrequencyRepository;

@Service
public class TimeOffAccrualDelegator {

  private final List<TimeOffAccrualService> accrualServices;

  private final TimeOffAccrualFrequencyRepository frequencyRepository;

  @Autowired
  public TimeOffAccrualDelegator(
      final List<TimeOffAccrualService> accrualServices,
      final TimeOffAccrualFrequencyRepository frequencyRepository) {
    this.accrualServices = accrualServices;
    this.frequencyRepository = frequencyRepository;
  }

  public TimeOffBreakdownDto getTimeOffBreakdown(
      final String frequencyTypeId, final TimeOffBreakdownCalculatePojo calculatePojo) {
    final TimeOffAccrualFrequency timeOffFrequency =
        frequencyRepository
            .findById(frequencyTypeId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        String.format("Time off frequency with id %s not found.", frequencyTypeId),
                        frequencyTypeId,
                        "time off frequency"));

    final TimeOffBreakdownYearDto startingBreakdown =
        TimeOffBreakdownItemDto.fromTimeOffPolicyUser(calculatePojo);

    final TimeOffAccrualService accrualService =
        accrualServices.stream()
            .filter(
                timeOffAccrualService -> timeOffAccrualService.support(timeOffFrequency.getName()))
            .findFirst()
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Can not find appropriate accrual strategy.", "accrual strategy"));

    return accrualService.getTimeOffBreakdown(startingBreakdown, calculatePojo);
  }
}
