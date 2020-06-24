package shamu.company.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class FeignAuthInterceptor implements RequestInterceptor {

  @Override
  public void apply(final RequestTemplate requestTemplate) {
    final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
      final HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
      requestTemplate.header(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
      requestTemplate.header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
      requestTemplate.header(
          HttpHeaders.AUTHORIZATION, request.getHeader(HttpHeaders.AUTHORIZATION));
    }
  }
}
