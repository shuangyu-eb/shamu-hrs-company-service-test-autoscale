package shamu.company.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import shamu.company.financialengine.FinancialEngineResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancialEngineHelperTest {
  public static MockWebServer mockBackEnd;
  private final ObjectMapper MAPPER = new ObjectMapper();
  private String baseUrl;
  private FinancialEngineHelper financialEngineHelper;
  private final WebClient webClient = WebClient.create();

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  static class MockRequestResult {
    private String name = "initName";
  }

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
    baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
    financialEngineHelper = new FinancialEngineHelper(webClient);
  }

  @Test
  void get() throws Exception {
    final FinancialEngineResponse<MockRequestResult> mockRequestResult =
        new FinancialEngineResponse<>();
    mockRequestResult.setBody(new MockRequestResult());
    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(mockRequestResult))
            .addHeader("Content-Type", "application/json"));
    final Mono<FinancialEngineResponse<MockRequestResult>> resultMono =
        financialEngineHelper.get(baseUrl);
    StepVerifier.create(resultMono).expectNextMatches(r -> r.getBody() != null).verifyComplete();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertEquals(RequestMethod.GET.name(), recordedRequest.getMethod());
    assertEquals("/", recordedRequest.getPath());
  }

  @Test
  void post() throws Exception {
    final FinancialEngineResponse<MockRequestResult> mockRequestResult =
        new FinancialEngineResponse<>();
    mockRequestResult.setBody(new MockRequestResult());
    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(mockRequestResult))
            .addHeader("Content-Type", "application/json"));
    final Mono<FinancialEngineResponse<MockRequestResult>> resultMono =
        financialEngineHelper.post(baseUrl, new MockRequestResult());
    StepVerifier.create(resultMono).expectNextMatches(r -> r.getBody() != null).verifyComplete();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertEquals(RequestMethod.POST.name(), recordedRequest.getMethod());
    assertEquals("/", recordedRequest.getPath());
  }

  @Test
  void put() throws Exception {
    final FinancialEngineResponse<MockRequestResult> mockRequestResult =
        new FinancialEngineResponse<>();
    mockRequestResult.setBody(new MockRequestResult());
    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(mockRequestResult))
            .addHeader("Content-Type", "application/json"));
    final Mono<FinancialEngineResponse<MockRequestResult>> resultMono =
        financialEngineHelper.put(baseUrl, new MockRequestResult());
    StepVerifier.create(resultMono).expectNextMatches(r -> r.getBody() != null).verifyComplete();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertEquals(RequestMethod.PUT.name(), recordedRequest.getMethod());
    assertEquals("/", recordedRequest.getPath());
  }

  @Test
  void delete() throws Exception {
    final FinancialEngineResponse<Void> mockRequestResult = new FinancialEngineResponse<>();
    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(mockRequestResult))
            .addHeader("Content-Type", "application/json"));
    final Mono<FinancialEngineResponse<Void>> resultMono = financialEngineHelper.delete(baseUrl);
    StepVerifier.create(resultMono).expectNext(mockRequestResult).verifyComplete();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertEquals(RequestMethod.DELETE.name(), recordedRequest.getMethod());
    assertEquals("/", recordedRequest.getPath());
  }
}
