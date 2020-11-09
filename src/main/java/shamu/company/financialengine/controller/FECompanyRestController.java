package shamu.company.financialengine.controller;

import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.financialengine.annotation.FinancialEngineRestController;
import shamu.company.financialengine.dto.CompanyInformationDto;
import shamu.company.financialengine.dto.IndustryDto;
import shamu.company.financialengine.dto.LegalEntityTypeDto;
import shamu.company.financialengine.dto.NewFECompanyInformationDto;
import shamu.company.financialengine.exception.FinancialEngineGenericException;
import shamu.company.financialengine.service.FECompanyService;

@FinancialEngineRestController
public class FECompanyRestController {
  private final FECompanyService feCompanyService;

  public FECompanyRestController(final FECompanyService feCompanyService) {
    this.feCompanyService = feCompanyService;
  }

  @GetMapping("available-industries")
  public List<IndustryDto> getAvailableIndustries() {
    return feCompanyService.getAvailableIndustries();
  }

  @GetMapping("company-info")
  public CompanyInformationDto getCompanyInformation() {
    return feCompanyService.getCompanyInformation();
  }

  @PostMapping("company")
  public HttpEntity saveFinancialEngine(
      @RequestBody final NewFECompanyInformationDto feCompanyInformationDto)
      throws FinancialEngineGenericException {
    feCompanyService.saveFinancialEngine(feCompanyInformationDto);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("available-legal-entity-types")
  public List<LegalEntityTypeDto> getLegalEntityTypes() {
    return feCompanyService.getLegalEntityTypes();
  }
}
