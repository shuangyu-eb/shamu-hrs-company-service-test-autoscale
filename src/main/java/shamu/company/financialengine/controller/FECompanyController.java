package shamu.company.financialengine.controller;

import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shamu.company.financialengine.dto.BankConnectionWidgetDto;
import shamu.company.financialengine.dto.CompanyInformationDto;
import shamu.company.financialengine.dto.IndustryDto;
import shamu.company.financialengine.dto.LegalEntityTypeDto;
import shamu.company.financialengine.dto.NewFECompanyInformationDto;
import shamu.company.financialengine.service.FECompanyService;

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

  @GetMapping("/info")
  public CompanyInformationDto getCompanyInformation() {
    return feCompanyService.getCompanyInformation();
  }

  @PostMapping("/save")
  public HttpEntity saveFinancialEngine(
      @RequestBody final NewFECompanyInformationDto feCompanyInformationDto) throws Exception {
    feCompanyService.saveFinancialEngine(feCompanyInformationDto);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/legal-entity-types")
  public List<LegalEntityTypeDto> getLegalEntityTypes() {
    return feCompanyService.getLegalEntityTypes();
  }

  @GetMapping("/bank/connection-widget")
  public BankConnectionWidgetDto getBankConnect() {
    return feCompanyService.getBankConnection();
  }
}
