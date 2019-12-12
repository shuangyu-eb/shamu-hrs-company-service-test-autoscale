package shamu.company.common.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.StateProvinceRepository;
import shamu.company.utils.ReflectionUtil;

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

  public List<CommonDictionaryDto> findAllByCountry(String id) {
    Country country = new Country();
    country.setId(id);

    List<StateProvince> stateProvinces = stateProvinceRepository.findAllByCountry(country);
    return ReflectionUtil.convertTo(stateProvinces, CommonDictionaryDto.class);
  }
}
