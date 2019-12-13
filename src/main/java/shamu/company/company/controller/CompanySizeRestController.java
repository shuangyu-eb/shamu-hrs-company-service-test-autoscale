package shamu.company.company.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.company.service.CompanyService;

@RestApiController
public class CompanySizeRestController {

  private final CompanyService companyService;

  @Autowired
  public CompanySizeRestController(final CompanyService companyService) {
    this.companyService = companyService;
  }

  @GetMapping("company-sizes")
  public List<CommonDictionaryDto> getCompanySizes() {
    return companyService.getCompanySizes();
  }
}
