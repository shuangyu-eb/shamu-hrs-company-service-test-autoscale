package shamu.company.financialengine.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shamu.company.financialengine.dto.IndustryDto;
import shamu.company.financialengine.service.FECompanyService;
import java.util.List;

@RestController
// TODO Consider adding an annotation like '@FinancialEngineRestApiController'
// TODO Haven't decided to extend from 'BaseController'
@RequestMapping("/company/financial-engine")
public class FECompanyController {
  private final FECompanyService feCompanyService;

  public FECompanyController(final FECompanyService feCompanyService) {
    this.feCompanyService = feCompanyService;
  }

  @GetMapping("/available-industries")
  public List<IndustryDto> getAvailableIndustries() {
    return feCompanyService.getAvailableIndustries();
  }
}
