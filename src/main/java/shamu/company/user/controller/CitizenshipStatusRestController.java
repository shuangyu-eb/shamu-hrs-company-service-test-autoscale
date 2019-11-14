package shamu.company.user.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.repository.CitizenshipStatusRepository;
import shamu.company.user.entity.CitizenshipStatus;
import shamu.company.utils.ReflectionUtil;

@RestApiController
public class CitizenshipStatusRestController {

  private final CitizenshipStatusRepository citizenshipStatusRepository;

  @Autowired
  public CitizenshipStatusRestController(
      final CitizenshipStatusRepository citizenshipStatusRepository) {
    this.citizenshipStatusRepository = citizenshipStatusRepository;
  }

  @GetMapping("citizenship-statuses")
  public List<CommonDictionaryDto> getCitizenshipStatuses() {

    List<CitizenshipStatus> citizenshipStatuses = citizenshipStatusRepository
        .findAll();
    return ReflectionUtil.convertTo(citizenshipStatuses, CommonDictionaryDto.class);
  }
}
