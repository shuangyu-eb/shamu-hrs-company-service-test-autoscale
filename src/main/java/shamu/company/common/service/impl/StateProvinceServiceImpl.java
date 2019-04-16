package shamu.company.common.service.impl;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.ResouceNotFoundException;
import shamu.company.common.repository.StateProvinceRepository;
import shamu.company.common.service.StateProvinceService;

@Service
public class StateProvinceServiceImpl implements StateProvinceService {

  @Autowired
  StateProvinceRepository stateProvinceRepository;

  @Override
  public StateProvince getStateProvince(Long id) {
    Optional<StateProvince> optionalStateProvince = stateProvinceRepository.findById(id);
    return optionalStateProvince.orElseThrow(
        () -> new ResouceNotFoundException("StateProvince does not exist"));
  }
}
