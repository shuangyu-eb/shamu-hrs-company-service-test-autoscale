package shamu.company.timeoff.repository;

import java.util.List;
import shamu.company.common.repository.BaseRepository;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;

public interface TimeOffAccrualFrequencyRepository extends
    BaseRepository<TimeOffAccrualFrequency, String> {

  List<TimeOffAccrualFrequency> findAll();
}
