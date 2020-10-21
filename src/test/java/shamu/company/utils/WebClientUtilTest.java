package shamu.company.utils;

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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebClientUtilTest {
  public static MockWebServer mockBackEnd;
  private final ObjectMapper MAPPER = new ObjectMapper();
  private final WebClient webClient = WebClient.create();
  private final ParameterizedTypeReference<MockRequestResult> parameterizedTypeReference =
      ParameterizedTypeReference.forType(MockRequestResult.class);
  private String baseUrl;
  final MockRequestResult mockResult = new MockRequestResult("mockRequestResult");

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
  }

  @Test
  void get() throws Exception {
    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(mockResult))
            .addHeader("Content-Type", "application/json"));
    final Mono<MockRequestResult> resultMono =
        WebClientUtil.get(webClient, parameterizedTypeReference, baseUrl);
    StepVerifier.create(resultMono)
        .expectNextMatches(result -> result.getName().equals("mockRequestResult"))
        .verifyComplete();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertEquals(RequestMethod.GET.name(), recordedRequest.getMethod());
    assertEquals("/", recordedRequest.getPath());
  }

  @Test
  void post() throws Exception {
    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(mockResult))
            .addHeader("Content-Type", "application/json"));
    final Mono<MockRequestResult> resultMono =
        WebClientUtil.post(webClient, parameterizedTypeReference, baseUrl, new MockRequestResult());
    StepVerifier.create(resultMono)
        .expectNextMatches(result -> result.getName().equals("mockRequestResult"))
        .verifyComplete();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertEquals(RequestMethod.POST.name(), recordedRequest.getMethod());
    assertEquals("/", recordedRequest.getPath());
  }

  @Test
  void put() throws Exception {
    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(mockResult))
            .addHeader("Content-Type", "application/json"));
    final Mono<MockRequestResult> resultMono =
        WebClientUtil.put(webClient, parameterizedTypeReference, baseUrl, new MockRequestResult());
    StepVerifier.create(resultMono)
        .expectNextMatches(result -> result.getName().equals("mockRequestResult"))
        .verifyComplete();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertEquals(RequestMethod.PUT.name(), recordedRequest.getMethod());
    assertEquals("/", recordedRequest.getPath());
  }

  @Test
  void delete() throws Exception {
    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(mockResult))
            .addHeader("Content-Type", "application/json"));
    final Mono<MockRequestResult> resultMono =
        WebClientUtil.delete(webClient, parameterizedTypeReference, baseUrl);
    StepVerifier.create(resultMono)
        .expectNextMatches(result -> result.getName().equals("mockRequestResult"))
        .verifyComplete();
    final RecordedRequest recordedRequest = mockBackEnd.takeRequest();
    assertEquals(RequestMethod.DELETE.name(), recordedRequest.getMethod());
    assertEquals("/", recordedRequest.getPath());
  }
}
