package shamu.company.common.config;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

@Configuration
public class RequestLoggingFilterConfig {

  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilterConfig.class);

  private static final int MAX_PAYLOAD_LENGTH = 10000;

  @Bean
  public Filter loggingFilter() {
    final AbstractRequestLoggingFilter filter = new AbstractRequestLoggingFilter() {

      @Override
      protected void beforeRequest(final HttpServletRequest request, final String message) {
      }

      @Override
      protected void afterRequest(final HttpServletRequest request, final String message) {
        if (!request.getRequestURI().contains("/health")) {
          log.info("{} {}", request.getMethod(), request.getRequestURI());
        }
      }
    };

    filter.setIncludeClientInfo(false);
    filter.setIncludePayload(false);
    filter.setIncludeQueryString(true);
    filter.setMaxPayloadLength(MAX_PAYLOAD_LENGTH);
    filter.setIncludeHeaders(false);
    filter.setAfterMessagePrefix("");

    return filter;
  }
}
