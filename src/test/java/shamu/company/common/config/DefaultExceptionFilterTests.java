package shamu.company.common.config;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import shamu.company.common.exception.EmailException;

public class DefaultExceptionFilterTests {

  private final DefaultExceptionFilter defaultExceptionFilter = new DefaultExceptionFilter();

  @Test
  void testDoFilter() throws IOException, ServletException {
    final ServletRequest servletRequest = new MockHttpServletRequest();
    final ServletResponse servletResponse = new MockHttpServletResponse();
    final FilterChain filterChain = Mockito.mock(FilterChain.class);
    Mockito.doThrow(new EmailException("error"))
        .when(filterChain)
        .doFilter(servletRequest, servletResponse);
    assertThatCode(
            () -> defaultExceptionFilter.doFilter(servletRequest, servletResponse, filterChain))
        .doesNotThrowAnyException();
  }
}
