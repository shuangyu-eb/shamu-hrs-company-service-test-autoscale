package shamu.company.financialengine.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.service.OfficeService;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.company.service.CompanyService;
import shamu.company.crypto.SecretHashRepository;
import shamu.company.financialengine.FinancialEngineResponse;
import shamu.company.financialengine.dto.AddNewFEAddressResponseDto;
import shamu.company.financialengine.dto.AddNewFECompanyResponseDto;
import shamu.company.financialengine.dto.BankAccountInfoDto;
import shamu.company.financialengine.dto.BankConnectionWidgetDto;
import shamu.company.financialengine.dto.GetBankConnectResponseDto;
import shamu.company.financialengine.dto.CompanyInformationDto;
import shamu.company.financialengine.dto.CompanyTaxIdDto;
import shamu.company.financialengine.dto.FECompanyDto;
import shamu.company.financialengine.dto.IndustryDto;
import shamu.company.financialengine.dto.LegalEntityTypeDto;
import shamu.company.financialengine.dto.NewFECompanyInformationDto;
import shamu.company.financialengine.dto.NewFinancialEngineAddressDto;
import shamu.company.financialengine.entity.FEAddresses;
import shamu.company.financialengine.entity.FEAddresses.FeAddressType;
import shamu.company.financialengine.entity.FECompany;
import shamu.company.financialengine.entity.FECompanyMapper;
import shamu.company.financialengine.repository.FEAddressRepository;
import shamu.company.financialengine.repository.FECompanyRepository;
import shamu.company.helpers.FinancialEngineHelper;
import shamu.company.sentry.SentryLogger;
import shamu.company.utils.Base64Utils;

@Service
public class FECompanyService {

  private final FinancialEngineHelper financialEngineHelper;
  private final FECompanyMapper feCompanyMapper;
  private final CompanyService companyService;
  private final FECompanyRepository feCompanyRepository;
  private final FEAddressRepository feAddressRepository;
  private final OfficeService officeService;

  private static final SentryLogger log = new SentryLogger(SecretHashRepository.class);

  public FECompanyService(
      final FinancialEngineHelper financialEngineHelper,
      final FECompanyMapper feCompanyMapper,
      final CompanyService companyService,
      final FECompanyRepository feCompanyRepository,
      final OfficeService officeService,
      final FEAddressRepository feAddressRepository) {
    this.financialEngineHelper = financialEngineHelper;
    this.feCompanyMapper = feCompanyMapper;
    this.companyService = companyService;
    this.feCompanyRepository = feCompanyRepository;
    this.officeService = officeService;
    this.feAddressRepository = feAddressRepository;
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

  public CompanyInformationDto getCompanyInformation() {
    final Company company = companyService.getCompany();
    FECompany feCompany = feCompanyRepository.findByCompanyId(company.getId());
    if (feCompany == null) {
      return new CompanyInformationDto();
    } else {
      final Mono<FinancialEngineResponse<CompanyInformationDto>> industriesMono =
          financialEngineHelper.get("/company/" + feCompany.getFeCompanyId());
      final FinancialEngineResponse<CompanyInformationDto> response = industriesMono.block();
      if (response == null) {
        return new CompanyInformationDto();
      }
      return response.getBody(CompanyInformationDto.class);
    }
  }

  public void saveFinancialEngine(final NewFECompanyInformationDto companyDetailsDto)
      throws Exception {
    String feCompanyId = saveFECompany(companyDetailsDto);
    log.info(feCompanyId);
    // TODO after /company/address/new normal, open it
    // saveFEAddress(companyDetailsDto, feCompanyId);
  }

  private String saveFECompany(final NewFECompanyInformationDto companyDetailsDto)
      throws Exception {
    final Company company = companyService.getCompany();
    FECompany feCompany = feCompanyRepository.findByCompanyId(company.getId());
    final FECompanyDto feCompanyDto = feCompanyMapper.convertFECompanyDto(companyDetailsDto);

    // Add new fe company
    if (null == feCompany) {
      feCompany = new FECompany();
      final Mono<FinancialEngineResponse<AddNewFECompanyResponseDto>> industriesMono =
          financialEngineHelper.post("/company/new", feCompanyDto);
      final FinancialEngineResponse<AddNewFECompanyResponseDto> response = industriesMono.block();
      if (response != null && !response.getSuccess()) {
        // TODO handle specific exception
        throw new Exception(response.getError());
      }
      final String feCompanyId =
          response.getBody(AddNewFECompanyResponseDto.class).getCompanyUuid();
      feCompany.setCompany(company);
      feCompany.setFeCompanyId(feCompanyId);
      feCompanyRepository.save(feCompany);
      return feCompanyId;
    }
    // Update fe company
    final Mono<FinancialEngineResponse<String>> industriesMono =
        financialEngineHelper.put(
            "/company/" + feCompany.getFeCompanyId() + "/update", feCompanyDto);
    final FinancialEngineResponse<String> response = industriesMono.block();
    if (response != null && !response.getSuccess()) {
      // TODO handle specific exception
      throw new Exception(response.getError());
    }
    return null;
  }

  private void saveFEAddress(
      final NewFECompanyInformationDto companyDetailsDto, final String feCompanyId)
      throws Exception {
    final Office mailOffice = officeService.findById(companyDetailsDto.getMailingAddress());
    final Office filingOffice = officeService.findById(companyDetailsDto.getFilingAddress());
    addNewOrUpdateFeAddress(feCompanyId, mailOffice.getOfficeAddress(), FeAddressType.MAILING);
    addNewOrUpdateFeAddress(feCompanyId, filingOffice.getOfficeAddress(), FeAddressType.FILING);
  }

  private void addNewOrUpdateFeAddress(
      final String feCompanyId, final OfficeAddress officeAddress, final FeAddressType addressType)
      throws Exception {
    FEAddresses feAddresses =
        feAddressRepository.findByOfficeAddressAndType(officeAddress, addressType);
    if (null == feAddresses) {
      feAddresses = new FEAddresses();
      final NewFinancialEngineAddressDto financialEngineAddressDto =
          feCompanyMapper.convertFinancialEngineAddressDto(feCompanyId, officeAddress, addressType);
      final Mono<FinancialEngineResponse<AddNewFEAddressResponseDto>> industriesMono =
          financialEngineHelper.post("/company/address/new", financialEngineAddressDto);
      final FinancialEngineResponse<AddNewFEAddressResponseDto> response = industriesMono.block();
      if (response != null && !response.getSuccess()) {
        throw new Exception(response.getError());
      }
      final String addressUuid =
          response.getBody(AddNewFEAddressResponseDto.class).getAddressUuid();
      feAddresses.setOfficeAddress(officeAddress);
      feAddresses.setFeAddressId(addressUuid);
      feAddresses.setType(addressType);
      feAddressRepository.save(feAddresses);
    }
  }

  // TODO FE api function error
  public List<CompanyTaxIdDto> getAvailableTaxList() {
    FECompany feCompany = findFeCompany();
    final Mono<FinancialEngineResponse<List<CompanyTaxIdDto>>> industriesMono =
        financialEngineHelper.get("/company/" + feCompany.getFeCompanyId() + "/available/tax-list");
    final FinancialEngineResponse<List<CompanyTaxIdDto>> response = industriesMono.block();
    if (response != null && response.getSuccess()) {
      return response.getBody();
    }
    return Collections.emptyList();
  }

  public FECompany findFeCompany() {
    final Company company = companyService.getCompany();
    FECompany feCompany = feCompanyRepository.findByCompanyId(company.getId());
    if (null == feCompany) {
      throw new ResourceNotFoundException(
          String.format("FE company with HRISCompanyId %s not found!", company.getId()),
          company.getId(),
          "fe company");
    }
    return feCompany;
  }

  public BankConnectionWidgetDto getBankConnection() {
    final Company company = companyService.getCompany();
    FECompany feCompany = feCompanyRepository.findByCompanyId(company.getId());
    final Mono<FinancialEngineResponse<GetBankConnectResponseDto>> bankConnectionMono =
        financialEngineHelper
            .get(String.format("/company/%s/bank/connect", feCompany.getFeCompanyId()));
    final FinancialEngineResponse<GetBankConnectResponseDto> response = bankConnectionMono.block();
    if (response != null) {
      GetBankConnectResponseDto bankConnectResponseDto = response
          .getBody(GetBankConnectResponseDto.class);
      String responseScript = Base64Utils.decode(bankConnectResponseDto.getWidgetHtml());
      return getUrlOfResponseScript(responseScript);
    }
    return null;
  }

  private BankConnectionWidgetDto getUrlOfResponseScript(String script) {
    String scrRegexp = "src=\"(.*)\"";
    BankConnectionWidgetDto bankConnectionWidgetDto = new BankConnectionWidgetDto();
    Pattern pattern = Pattern.compile(scrRegexp);
    Matcher matcher = pattern.matcher(script);
    if (matcher.find()) {
      bankConnectionWidgetDto.setOriginUrl(matcher.group(1));
    }
    String urlRegexp = "url: \"(.*)\"";
    pattern = Pattern.compile(urlRegexp);
    matcher = pattern.matcher(script);
    if (matcher.find()) {
      bankConnectionWidgetDto.setCompanyBankConnectUrl(matcher.group(1));
    }
    return bankConnectionWidgetDto;
  }

  public BankAccountInfoDto getBankAccountInfo() {
    final Company company = companyService.getCompany();
    FECompany feCompany = feCompanyRepository.findByCompanyId(company.getId());
    final Mono<FinancialEngineResponse<BankAccountInfoDto>> bankAccountMono =
        financialEngineHelper
            .get(String.format("/company/%s/bank/sync", feCompany.getFeCompanyId()));
    FinancialEngineResponse<BankAccountInfoDto> response = bankAccountMono.block();

    BankAccountInfoDto bankAccountInfoDto = new BankAccountInfoDto();
    if (response != null) {
      bankAccountInfoDto = response.getBody(BankAccountInfoDto.class);
    }
    return bankAccountInfoDto;
  }
}
