package shamu.company.utils;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public interface WebClientUtil {

  static <T> Mono<T> get(
      final WebClient webClient,
      final ParameterizedTypeReference<T> parameterizedTypeReference,
      final String uri) {
    return webClient
        .get()
        .uri(uri)
        .retrieve()
        // TODO The exception should support customization, @Lucas will refactor it
        .onStatus(HttpStatus::isError, ClientResponse::createException)
        .bodyToMono(parameterizedTypeReference);
  }

  static <T, U> Mono<T> post(
      final WebClient webClient,
      final ParameterizedTypeReference<T> parameterizedTypeReference,
      final String uri,
      final U requestBody) {
    return webClient
        .post()
        .uri(uri)
        .body(BodyInserters.fromValue(requestBody))
        .retrieve()
        .onStatus(HttpStatus::isError, ClientResponse::createException)
        .bodyToMono(parameterizedTypeReference);
  }

  static <T, U> Mono<T> put(
      final WebClient webClient,
      final ParameterizedTypeReference<T> parameterizedTypeReference,
      final String uri,
      final U requestBody) {
    return webClient
        .put()
        .uri(uri)
        .body(BodyInserters.fromValue(requestBody))
        .retrieve()
        .onStatus(HttpStatus::isError, ClientResponse::createException)
        .bodyToMono(parameterizedTypeReference);
  }

  static <T> Mono<T> delete(
      final WebClient webClient,
      final ParameterizedTypeReference<T> parameterizedTypeReference,
      final String uri) {
    return webClient
        .delete()
        .uri(uri)
        .retrieve()
        .onStatus(HttpStatus::isError, ClientResponse::createException)
        .bodyToMono(parameterizedTypeReference);
  }
}
