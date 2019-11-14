package shamu.company.common.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.repository.StateRepository;
import shamu.company.info.entity.State;
import shamu.company.utils.ReflectionUtil;

@RestApiController
public class StateRestController {

  private final StateRepository stateRepository;

  @Autowired
  public StateRestController(final StateRepository stateRepository) {
    this.stateRepository = stateRepository;
  }

  @GetMapping("states")
  public List<CommonDictionaryDto> getStates() {
    List<State> states = stateRepository.findAll();
    return ReflectionUtil.convertTo(states, CommonDictionaryDto.class);
  }
}
