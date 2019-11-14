package shamu.company.company.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.company.entity.CompanySize;
import shamu.company.company.repository.CompanySizeRepository;
import shamu.company.utils.ReflectionUtil;

@RestApiController
public class CompanySizeRestController {

  private final CompanySizeRepository companySizeRepository;
  
  @Autowired
  public CompanySizeRestController(final CompanySizeRepository companySizeRepository) {
    this.companySizeRepository = companySizeRepository;
  }
  
  @GetMapping("company-sizes")
  public List<CommonDictionaryDto> getCompanySizes() {
    List<CompanySize> companySizes = companySizeRepository.findAll();
    return ReflectionUtil.convertTo(companySizes, CommonDictionaryDto.class);
  }
}
