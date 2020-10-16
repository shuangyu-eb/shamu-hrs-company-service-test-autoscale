package shamu.company.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FinancialEngineConfiguration {
  private static final String X_TENANT_ID = "x-tenantid";

  private static final String X_TENANT_SECRET = "x-tenantsecret";

  private final FinancialEngineProperties properties;

  public FinancialEngineConfiguration(final FinancialEngineProperties properties) {
    this.properties = properties;
  }

  @Bean
  public WebClient webClient() {
    return WebClient.builder()
        .baseUrl(properties.getBaseUrl())
        .defaultHeaders(
            httpHeaders -> {
              httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
              httpHeaders.set(X_TENANT_ID, properties.getTenantId());
              httpHeaders.set(X_TENANT_SECRET, properties.getTenantSecret());
            })
        .build();
  }
}
