package shamu.company.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "application")
public class ApplicationConfig {

  private String systemEmailAddress;

  private String frontEndAddress;

  private String helpUrl;
}
