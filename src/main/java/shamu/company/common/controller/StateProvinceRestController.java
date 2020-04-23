package shamu.company.common.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.service.StateProvinceService;

@RestApiController
public class StateProvinceRestController {

  private final StateProvinceService stateProvinceService;

  @Autowired
  public StateProvinceRestController(final StateProvinceService stateProvinceService) {
    this.stateProvinceService = stateProvinceService;
  }

  @GetMapping("country/{id}/state-provinces")
  public List<CommonDictionaryDto> findAllStateProvincesByCountry(@PathVariable String id) {
    return stateProvinceService.findAllByCountry(id);
  }
}
