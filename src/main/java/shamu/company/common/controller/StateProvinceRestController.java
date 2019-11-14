package shamu.company.common.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.repository.StateProvinceRepository;
import shamu.company.utils.ReflectionUtil;

@RestApiController
public class StateProvinceRestController {

  private final StateProvinceRepository stateProvinceRepository;

  @Autowired
  public StateProvinceRestController(final StateProvinceRepository stateProvinceRepository) {
    this.stateProvinceRepository = stateProvinceRepository;
  }

  @GetMapping("country/{id}/state-provinces")
  public List<CommonDictionaryDto> getAllStateProvincesByCountry(@PathVariable String id) {
    Country country = new Country();
    country.setId(id);

    List<StateProvince> stateProvinces = stateProvinceRepository.findAllByCountry(country);
    return ReflectionUtil.convertTo(stateProvinces, CommonDictionaryDto.class);
  }
}
