package shamu.company.financialengine.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.financialengine.FinancialEngineResponse;
import shamu.company.financialengine.dto.BankAccountInfoDto;
import shamu.company.financialengine.dto.BankConnectionWidgetDto;
import shamu.company.financialengine.dto.GetBankConnectResponseDto;
import shamu.company.financialengine.entity.FECompany;
import shamu.company.financialengine.repository.FECompanyRepository;
import shamu.company.helpers.FinancialEngineHelper;
import shamu.company.utils.Base64Utils;

/** @author Lucas */
@Service
public class FEBankService {

  private final FinancialEngineHelper financialEngineHelper;
  private final CompanyService companyService;
  private final FECompanyRepository feCompanyRepository;

  public FEBankService(
      final FinancialEngineHelper financialEngineHelper,
      final CompanyService companyService,
      final FECompanyRepository feCompanyRepository) {
    this.financialEngineHelper = financialEngineHelper;
    this.companyService = companyService;
    this.feCompanyRepository = feCompanyRepository;
  }

  public BankConnectionWidgetDto getCompanyBankConnection() {
    final Company company = companyService.getCompany();
    final FECompany feCompany = feCompanyRepository.findByCompanyId(company.getId());
    final Mono<FinancialEngineResponse<GetBankConnectResponseDto>> bankConnectionMono =
        financialEngineHelper.get(
            String.format("/company/%s/bank/connect", feCompany.getFeCompanyId()));
    final FinancialEngineResponse<GetBankConnectResponseDto> response = bankConnectionMono.block();
    if (response != null) {
      final GetBankConnectResponseDto bankConnectResponseDto =
          response.getBody(GetBankConnectResponseDto.class);
      final String responseScript = Base64Utils.decode(bankConnectResponseDto.getWidgetHtml());
      return getUrlOfResponseScript(responseScript);
    }
    return null;
  }

  private BankConnectionWidgetDto getUrlOfResponseScript(final String script) {
    final String scrRegexp = "src=\"(.*)\"";
    final BankConnectionWidgetDto bankConnectionWidgetDto = new BankConnectionWidgetDto();
    Pattern pattern = Pattern.compile(scrRegexp);
    Matcher matcher = pattern.matcher(script);
    if (matcher.find()) {
      bankConnectionWidgetDto.setOriginUrl(matcher.group(1));
    }
    final String urlRegexp = "url: \"(.*)\"";
    pattern = Pattern.compile(urlRegexp);
    matcher = pattern.matcher(script);
    if (matcher.find()) {
      bankConnectionWidgetDto.setCompanyBankConnectUrl(matcher.group(1));
    }
    return bankConnectionWidgetDto;
  }

  public BankAccountInfoDto getCompanyBankAccountInfo() {
    final Company company = companyService.getCompany();
    final FECompany feCompany = feCompanyRepository.findByCompanyId(company.getId());
    final Mono<FinancialEngineResponse<BankAccountInfoDto>> bankAccountMono =
        financialEngineHelper.get(
            String.format("/company/%s/bank/sync", feCompany.getFeCompanyId()));
    final FinancialEngineResponse<BankAccountInfoDto> response = bankAccountMono.block();

    BankAccountInfoDto bankAccountInfoDto = new BankAccountInfoDto();
    if (response != null) {
      bankAccountInfoDto = response.getBody(BankAccountInfoDto.class);
    }
    return bankAccountInfoDto;
  }
}
