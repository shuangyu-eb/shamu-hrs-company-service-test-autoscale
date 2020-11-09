package shamu.company.financialengine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.WebClient;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.financialengine.FinancialEngineResponse;
import shamu.company.financialengine.WebFluxWebServer;
import shamu.company.financialengine.dto.BankAccountInfoDto;
import shamu.company.financialengine.dto.BankConnectionDto;
import shamu.company.financialengine.dto.BankConnectionWidgetDto;
import shamu.company.financialengine.dto.GetBankConnectResponseDto;
import shamu.company.financialengine.entity.FECompany;
import shamu.company.financialengine.repository.FECompanyRepository;
import shamu.company.helpers.FinancialEngineHelper;
import shamu.company.utils.UuidUtil;

/** @author Lucas */
class FEBankServiceTests extends WebFluxWebServer {
  private FEBankService feBankService;
  @Mock private CompanyService companyService;
  @Mock private FECompanyRepository feCompanyRepository;

  @BeforeEach
  void initialize() {
    final String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
    final WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
    MockitoAnnotations.initMocks(this);
    feBankService =
        new FEBankService(
            new FinancialEngineHelper(webClient), companyService, feCompanyRepository);
  }

  @Test
  void testGetBankConnection() throws Exception {
    final FECompany feCompany = new FECompany();
    feCompany.setFeCompanyId("test");
    final Company company = new Company();
    company.setId(UuidUtil.getUuidString());
    feCompany.setCompany(company);
    Mockito.when(companyService.getCompany()).thenReturn(company);
    Mockito.when(feCompanyRepository.findByCompanyId(company.getId())).thenReturn(feCompany);
    final FinancialEngineResponse<GetBankConnectResponseDto> financialEngineResponse =
        new FinancialEngineResponse<>();
    final GetBankConnectResponseDto getBankConnectResponseDto = new GetBankConnectResponseDto();
    getBankConnectResponseDto.setWidgetHtml(
        Base64.getEncoder().encodeToString("src=\"test1\" \n url: \"test2\"".getBytes()));
    financialEngineResponse.setBody(getBankConnectResponseDto);
    financialEngineResponse.setSuccess(true);
    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(financialEngineResponse))
            .addHeader("Content-Type", "application/json"));
    final BankConnectionWidgetDto result = feBankService.getCompanyBankConnection();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertEquals(RequestMethod.GET.name(), recordedRequest.getMethod());
    assertEquals(
        "/company/" + feCompany.getFeCompanyId() + "/bank/connect", recordedRequest.getPath());
    assertEquals("test1", result.getOriginUrl());
    assertEquals("test2", result.getCompanyBankConnectUrl());
  }

  @Test
  void testGetBankAccountInfo() throws Exception {
    final FECompany feCompany = new FECompany();
    feCompany.setFeCompanyId("test");
    final Company company = new Company();
    company.setId(UuidUtil.getUuidString());
    feCompany.setCompany(company);
    Mockito.when(companyService.getCompany()).thenReturn(company);
    Mockito.when(feCompanyRepository.findByCompanyId(company.getId())).thenReturn(feCompany);

    final FinancialEngineResponse<BankAccountInfoDto> financialEngineResponse =
        new FinancialEngineResponse<>();
    final BankAccountInfoDto bankAccountInfoDto = new BankAccountInfoDto();
    final List<BankConnectionDto> bankConnections = new ArrayList<>();
    bankConnections.add(new BankConnectionDto());
    bankAccountInfoDto.setBankConnections(bankConnections);
    financialEngineResponse.setBody(bankAccountInfoDto);
    financialEngineResponse.setSuccess(true);

    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(financialEngineResponse))
            .addHeader("Content-Type", "application/json"));
    final BankAccountInfoDto result = feBankService.getCompanyBankAccountInfo();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();

    assertEquals(
        "/company/" + feCompany.getFeCompanyId() + "/bank/sync", recordedRequest.getPath());

    assertEquals(result.getBankConnections().size(), bankConnections.size());
  }
}
