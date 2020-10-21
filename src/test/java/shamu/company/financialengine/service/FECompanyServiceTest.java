package shamu.company.financialengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.WebClient;
import shamu.company.financialengine.FinancialEngineResponse;
import shamu.company.financialengine.dto.IndustryDto;
import shamu.company.helpers.FinancialEngineHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FECompanyServiceTest {
  public static MockWebServer mockBackEnd;
  private final ObjectMapper MAPPER = new ObjectMapper();
  private FECompanyService feCompanyService;

  @BeforeAll
  static void setUp() throws IOException {
    mockBackEnd = new MockWebServer();
    mockBackEnd.start();
  }

  @AfterAll
  static void tearDown() throws IOException {
    mockBackEnd.shutdown();
  }

  @BeforeEach
  void initialize() {
    final String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
    final WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
    feCompanyService = new FECompanyService(new FinancialEngineHelper(webClient));
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
}
