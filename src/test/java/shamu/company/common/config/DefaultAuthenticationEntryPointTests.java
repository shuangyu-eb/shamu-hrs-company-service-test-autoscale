package shamu.company.common.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultAuthenticationEntryPointTests {

  private final DefaultAuthenticationEntryPoint defaultAuthenticationEntryPoint = new DefaultAuthenticationEntryPoint();

  @Test
  void testCommence() {
    final HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
    final AuthenticationException e = Mockito.mock(AuthenticationException.class);
    Assertions.assertDoesNotThrow(() ->
        defaultAuthenticationEntryPoint.commence(httpServletRequest,httpServletResponse,e));

  }
}
