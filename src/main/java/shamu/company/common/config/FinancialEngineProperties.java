package shamu.company.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "financial")
public class FinancialEngineProperties {
  private String tenantId;

  private String tenantSecret;

  private String baseUrl;
}
