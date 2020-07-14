package shamu.company.common.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.ObjectMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

@Configuration
@Log4j2
public class RequestLoggingFilterConfig {

  private static final int MAX_PAYLOAD_LENGTH = 10000;

  @Bean
  public Filter loggingFilter(final Environment env) {
    final AbstractRequestLoggingFilter filter =
        new AbstractRequestLoggingFilter() {

          private static final String CORRELATION_ID_HEADER_NAME = "X-HRIS-API-Correlation-Id";
          private static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

          @Override
          protected void beforeRequest(final HttpServletRequest request, final String message) {
            final String correlationId = getCorrelationIdFromHeader(request);
            ThreadContext.put(CORRELATION_ID_LOG_VAR_NAME, correlationId);
            logRequest(request);
          }

          @Override
          protected void afterRequest(final HttpServletRequest request, final String message) {
            ThreadContext.remove(CORRELATION_ID_LOG_VAR_NAME);
          }

          private String getCorrelationIdFromHeader(final HttpServletRequest request) {
            String correlationId = request.getHeader(CORRELATION_ID_HEADER_NAME);
            if (StringUtils.isBlank(correlationId)) {
              correlationId = generateUniqueCorrelationId();
            }
            return correlationId;
          }

          private String generateUniqueCorrelationId() {
            return UUID.randomUUID().toString();
          }

          private void logRequest(final HttpServletRequest request) {
            if (!request.getRequestURI().contains("/health")) {
              final List profiles = Arrays.asList(env.getActiveProfiles());
              if (profiles.contains("local")) {
                log.info("{} {}", request.getMethod(), request.getRequestURI());
                return;
              }
              final Map<String, String> map = new TreeMap<>();
              map.put("httpEndpoint", request.getRequestURI());
              map.put("httpMethod", request.getMethod());
              map.put("userIP", request.getHeader("X-Forwarded-For"));
              map.put("logType", "Request");
              map.put("referer", request.getHeader("Referer"));
              map.put("userAgent", request.getHeader("User-Agent"));
              map.put("userIdentifier", request.getRemoteUser());
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
