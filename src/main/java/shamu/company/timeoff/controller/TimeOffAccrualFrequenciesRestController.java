package shamu.company.timeoff.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.repository.TimeOffAccrualFrequencyRepository;
import shamu.company.utils.ReflectionUtil;

@RestApiController
public class TimeOffAccrualFrequenciesRestController {

  private final TimeOffAccrualFrequencyRepository accrualFrequencyRepository;

  @Autowired
  public TimeOffAccrualFrequenciesRestController(
      final TimeOffAccrualFrequencyRepository frequencyRepository) {
    this.accrualFrequencyRepository = frequencyRepository;
  }

  @GetMapping("time-off-accrual-frequencies")
  public List<CommonDictionaryDto> getAllTimeOffFrequencies() {
    List<TimeOffAccrualFrequency> frequencies = accrualFrequencyRepository.findAll();
    return ReflectionUtil.convertTo(frequencies, CommonDictionaryDto.class);
  }
}
