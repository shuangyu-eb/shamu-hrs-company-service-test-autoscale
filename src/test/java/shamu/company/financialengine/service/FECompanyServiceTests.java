package shamu.company.financialengine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.WebClient;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.service.OfficeService;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyService;
import shamu.company.financialengine.FinancialEngineResponse;
import shamu.company.financialengine.WebFluxWebServer;
import shamu.company.financialengine.dto.AddNewFECompanyResponseDto;
import shamu.company.financialengine.dto.CompanyInformationDto;
import shamu.company.financialengine.dto.CompanyTaxIdDto;
import shamu.company.financialengine.dto.FECompanyDto;
import shamu.company.financialengine.dto.IndustryDto;
import shamu.company.financialengine.dto.LegalEntityTypeDto;
import shamu.company.financialengine.dto.NewFECompanyInformationDto;
import shamu.company.financialengine.entity.FECompany;
import shamu.company.financialengine.entity.FECompanyMapper;
import shamu.company.financialengine.repository.FEAddressRepository;
import shamu.company.financialengine.repository.FECompanyRepository;
import shamu.company.helpers.FinancialEngineHelper;
import shamu.company.utils.UuidUtil;

class FECompanyServiceTests extends WebFluxWebServer {
  private FECompanyService feCompanyService;
  @Mock private FECompanyMapper feCompanyMapper;
  @Mock private CompanyService companyService;
  @Mock private FECompanyRepository feCompanyRepository;
  @Mock private FEAddressRepository feAddressRepository;
  @Mock private OfficeService officeService;

  @BeforeEach
  void initialize() {
    final String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
    final WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
    MockitoAnnotations.initMocks(this);
    feCompanyService =
        new FECompanyService(
            new FinancialEngineHelper(webClient),
            feCompanyMapper,
            companyService,
            feCompanyRepository,
            officeService,
            feAddressRepository);
  }

  @Test
  void testGetAvailableIndustries() throws Exception {
    final List<IndustryDto> industries = new ArrayList<>();
    industries.add(new IndustryDto());
    final FinancialEngineResponse<List<IndustryDto>> financialEngineResponse =
        new FinancialEngineResponse<>();
    financialEngineResponse.setBody(industries);
    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(financialEngineResponse))
            .addHeader("Content-Type", "application/json"));
    final List<IndustryDto> results = feCompanyService.getAvailableIndustries();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertEquals(RequestMethod.GET.name(), recordedRequest.getMethod());
    assertEquals("/company/available/industries", recordedRequest.getPath());
    assertEquals(results.size(), financialEngineResponse.getBody().size());
  }

  @Test
  void testGetCompanyInformation() throws Exception {
    final Company company = new Company();
    company.setId(UuidUtil.getUuidString());
    final FECompany feCompany = new FECompany();
    feCompany.setCompany(company);
    feCompany.setFeCompanyId(UuidUtil.getUuidString());

    Mockito.when(companyService.getCompany()).thenReturn(company);
    Mockito.when(feCompanyRepository.findByCompanyId(company.getId())).thenReturn(feCompany);

    final CompanyInformationDto companyInformationDto = new CompanyInformationDto();
    final FinancialEngineResponse<CompanyInformationDto> financialEngineResponse =
        new FinancialEngineResponse<>();
    financialEngineResponse.setBody(companyInformationDto);
    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(financialEngineResponse))
            .addHeader("Content-Type", "application/json"));
    final CompanyInformationDto results = feCompanyService.getCompanyInformation();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertEquals(RequestMethod.GET.name(), recordedRequest.getMethod());
    assertEquals("/company/" + feCompany.getFeCompanyId(), recordedRequest.getPath());
    assertEquals(results.getIndustry(), financialEngineResponse.getBody().getIndustry());
  }

  @Test
  void testGetCompanyInformation_when_FeCompanyIsNull_thenReturnDto() {
    final Company company = new Company();
    company.setId(UuidUtil.getUuidString());

    Mockito.when(companyService.getCompany()).thenReturn(company);
    Mockito.when(feCompanyRepository.findByCompanyId(company.getId())).thenReturn(null);

    assertThat(feCompanyService.getCompanyInformation()).isInstanceOf(CompanyInformationDto.class);
  }

  @Nested
  class saveFinancialEngine {
    private Company company;
    private NewFECompanyInformationDto companyInformationDto;
    private final String companyUuid = UuidUtil.getUuidString();

    @BeforeEach
    void init() {
      company = new Company();
      company.setId(UuidUtil.getUuidString());

      companyInformationDto = new NewFECompanyInformationDto();
      companyInformationDto.setMailingAddress(UuidUtil.getUuidString());

      final FECompanyDto newCompanyDto = new FECompanyDto();

      Mockito.when(companyService.getCompany()).thenReturn(company);
      Mockito.when(feCompanyMapper.convertFECompanyDto(companyInformationDto))
          .thenReturn(newCompanyDto);
    }

    @Test
    void whenResponseIsNull_orResponseHasError_thenThrowException() {}

    @Test
    void when_not_exist_feCompany_then_addNew() throws Exception {
      final AddNewFECompanyResponseDto addNewFECompanyResponseDto =
          new AddNewFECompanyResponseDto();
      addNewFECompanyResponseDto.setCompanyUuid(companyUuid);

      final FinancialEngineResponse<AddNewFECompanyResponseDto> financialEngineResponse =
          new FinancialEngineResponse<>();
      financialEngineResponse.setBody(addNewFECompanyResponseDto);
      financialEngineResponse.setSuccess(true);
      mockBackEnd.enqueue(
          new MockResponse()
              .setBody(MAPPER.writeValueAsString(financialEngineResponse))
              .addHeader("Content-Type", "application/json"));
      feCompanyService.saveFinancialEngine(companyInformationDto);
      final RecordedRequest recordedRequest = mockBackEnd.takeRequest();

      assertEquals(RequestMethod.POST.name(), recordedRequest.getMethod());
      assertEquals(
          companyUuid,
          financialEngineResponse.getBody(AddNewFECompanyResponseDto.class).getCompanyUuid());
    }

    @Test
    void when_existFeCompany_then_shouldUpdate() throws Exception {
      final FECompany feCompany = new FECompany();
      feCompany.setCompany(company);

      Mockito.when(feCompanyRepository.findByCompanyId(company.getId())).thenReturn(feCompany);
      Mockito.when(companyService.getCompany()).thenReturn(company);

      final FinancialEngineResponse<String> financialEngineResponse =
          new FinancialEngineResponse<>();
      financialEngineResponse.setSuccess(true);

      mockBackEnd.enqueue(
          new MockResponse()
              .setBody(MAPPER.writeValueAsString(financialEngineResponse))
              .setHeader("Content-Type", "application/json"));

      feCompanyService.saveFinancialEngine(companyInformationDto);

      final RecordedRequest recordedRequest = mockBackEnd.takeRequest();

      Mockito.verify(feCompanyRepository, Mockito.times(1)).findByCompanyId(Mockito.any());
    }
  }

  @Test
  void testGetLegalEntityTypes() throws Exception {
    final List<IndustryDto> industries = new ArrayList<>();
    industries.add(new IndustryDto());
    final FinancialEngineResponse<List<IndustryDto>> financialEngineResponse =
        new FinancialEngineResponse<>();
    financialEngineResponse.setBody(industries);

    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(financialEngineResponse))
            .addHeader("Content-Type", "application/json"));
    final List<LegalEntityTypeDto> results = feCompanyService.getLegalEntityTypes();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertEquals(RequestMethod.GET.name(), recordedRequest.getMethod());
    assertEquals("/company/available/legal-entity-types", recordedRequest.getPath());
    assertEquals(results.size(), financialEngineResponse.getBody().size());
  }

  @Test
  void testGetAvailableTaxList() throws Exception {
    final FECompany feCompany = new FECompany();
    feCompany.setFeCompanyId("test");
    final Company company = new Company();
    company.setId(UuidUtil.getUuidString());
    feCompany.setCompany(company);
    Mockito.when(companyService.getCompany()).thenReturn(company);
    Mockito.when(feCompanyRepository.findByCompanyId(company.getId())).thenReturn(feCompany);

    final List<CompanyTaxIdDto> companyTaxIdDtos = new ArrayList<>();
    final CompanyTaxIdDto companyTaxIdDto = new CompanyTaxIdDto();
    companyTaxIdDtos.add(companyTaxIdDto);
    companyTaxIdDtos.add(new CompanyTaxIdDto());
    final FinancialEngineResponse<List<CompanyTaxIdDto>> financialEngineResponse =
        new FinancialEngineResponse<>();
    financialEngineResponse.setBody(companyTaxIdDtos);
    financialEngineResponse.setSuccess(true);

    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(financialEngineResponse))
            .addHeader("Content-Type", "application/json"));
    final List<CompanyTaxIdDto> results = feCompanyService.getAvailableTaxList();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertEquals(RequestMethod.GET.name(), recordedRequest.getMethod());
    assertEquals(
        "/company/" + feCompany.getFeCompanyId() + "/available/tax-list",
        recordedRequest.getPath());
    assertEquals(results.size(), financialEngineResponse.getBody().size());
  }

  @Test
  void testGetAvailableTaxList_whenFECompanyIsNull_thenThrowResourceNotFoundException() {
    final Company company = new Company();
    Mockito.when(companyService.getCompany()).thenReturn(company);
    Mockito.when(feCompanyRepository.findByCompanyId(company.getId())).thenReturn(null);
    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> feCompanyService.getAvailableTaxList());
  }
}
