package shamu.company.user.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.user.repository.CompensationFrequencyRepository;

@Service
@Transactional
public class CompensationFrequencyService {

  private final CompensationFrequencyRepository compensationFrequencyRepository;

  @Autowired
  public CompensationFrequencyService(
      final CompensationFrequencyRepository compensationFrequencyRepository) {
    this.compensationFrequencyRepository = compensationFrequencyRepository;
  }

  public CompensationFrequency findById(final String id) {
    return compensationFrequencyRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Compensation frequency type with id %s not found!", id)));
  }

  public List<CompensationFrequency> findAll() {
    return compensationFrequencyRepository.findAll();
  }
}
