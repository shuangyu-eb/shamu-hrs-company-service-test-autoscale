package shamu.company.common.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

@Configuration
public class RequestLoggingFilterConfig {

  private static final Logger log = LogManager.getLogger(RequestLoggingFilterConfig.class);

  private static final int MAX_PAYLOAD_LENGTH = 10000;

  @Bean
  public Filter loggingFilter(final Environment env) {
    final AbstractRequestLoggingFilter filter =
        new AbstractRequestLoggingFilter() {

          @Override
          protected void beforeRequest(final HttpServletRequest request, final String message) {
            // do nothing
          }

          @Override
          protected void afterRequest(final HttpServletRequest request, final String message) {
            if (!request.getRequestURI().contains("/health")) {
              final List profiles = Arrays.asList(env.getActiveProfiles());
              if (profiles.contains("local")) {
                  log.info("{} {}", request.getMethod(), request.getRequestURI());
                  return;
              }
              final Map<String, String> map = new TreeMap<>();
              map.put("HTTP Endpoint", request.getRequestURI());
              map.put("HTTP Method", request.getMethod());
              map.put("IP", request.getHeader("X-Forwarded-For"));
              map.put("Log Type", "Request");
              map.put("Referer", request.getHeader("Referer"));
              map.put("User Agent", request.getHeader("User-Agent"));
              map.put("User Identifier", request.getRemoteUser());
              final ObjectMessage msg = new ObjectMessage(map);
              log.info(msg);
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
