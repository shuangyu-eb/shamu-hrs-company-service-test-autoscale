package shamu.company.common.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.StateProvinceRepository;

@Service
public class StateProvinceService {

  private final StateProvinceRepository stateProvinceRepository;

  @Autowired
  public StateProvinceService(final StateProvinceRepository stateProvinceRepository) {
    this.stateProvinceRepository = stateProvinceRepository;
  }

  public StateProvince findById(final String id) {
    final Optional<StateProvince> optionalStateProvince = stateProvinceRepository.findById(id);
    return optionalStateProvince.orElseThrow(
        () -> new ResourceNotFoundException(
            String.format("StateProvince with id %s not found!", id)));
  }
}
