package shamu.company.financialengine.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import shamu.company.financialengine.FinancialEngineResponse;
import shamu.company.financialengine.dto.IndustryDto;
import shamu.company.financialengine.dto.LegalEntityTypeDto;
import shamu.company.helpers.FinancialEngineHelper;

import java.util.ArrayList;
import java.util.List;

@Service
public class FECompanyService {
  private final FinancialEngineHelper financialEngineHelper;

  public FECompanyService(final FinancialEngineHelper financialEngineHelper) {
    this.financialEngineHelper = financialEngineHelper;
  }

  public List<IndustryDto> getAvailableIndustries() {
    final Mono<FinancialEngineResponse<List<IndustryDto>>> industriesMono =
        financialEngineHelper.get("/company/available/industries");
    final FinancialEngineResponse<List<IndustryDto>> response = industriesMono.block();

    if (response == null) {
      return new ArrayList<>();
    }
    return response.getBody();
  }

  public List<LegalEntityTypeDto> getLegalEntityTypes() {
    final Mono<FinancialEngineResponse<List<LegalEntityTypeDto>>> industriesMono =
        financialEngineHelper.get("/company/available/legal-entity-types");
    final FinancialEngineResponse<List<LegalEntityTypeDto>> response = industriesMono.block();

    if (response == null) {
      return new ArrayList<>();
    }
    return response.getBody();
  }
}
