package shamu.company.helpers;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import shamu.company.financialengine.FinancialEngineResponse;
import shamu.company.utils.WebClientUtil;

// TODO add tests
@Component
public class FinancialEngineHelper {
  private final WebClient webClient;

  public FinancialEngineHelper(final WebClient webClient) {
    this.webClient = webClient;
  }

  public <T> Mono<FinancialEngineResponse<T>> get(final String uri) {
    final ParameterizedTypeReference<FinancialEngineResponse<T>> parameterizedTypeReference =
        ParameterizedTypeReference.forType(FinancialEngineResponse.class);
    return WebClientUtil.get(webClient, parameterizedTypeReference, uri);
  }

  public <T, U> Mono<FinancialEngineResponse<T>> post(final String uri, final U requestBody) {
    final ParameterizedTypeReference<FinancialEngineResponse<T>> parameterizedTypeReference =
        ParameterizedTypeReference.forType(FinancialEngineResponse.class);
    return WebClientUtil.post(webClient, parameterizedTypeReference, uri, requestBody);
  }

  public <T, U> Mono<FinancialEngineResponse<T>> put(final String uri, final U requestBody) {
    final ParameterizedTypeReference<FinancialEngineResponse<T>> parameterizedTypeReference =
        ParameterizedTypeReference.forType(FinancialEngineResponse.class);
    return WebClientUtil.put(webClient, parameterizedTypeReference, uri, requestBody);
  }

  public Mono<FinancialEngineResponse<Void>> delete(final String uri) {
    final ParameterizedTypeReference<FinancialEngineResponse<Void>> parameterizedTypeReference =
        ParameterizedTypeReference.forType(FinancialEngineResponse.class);
    return WebClientUtil.delete(webClient, parameterizedTypeReference, uri);
  }
}
