package shamu.company.common.config;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthorizationFilter implements Filter {

  private final JwtTokenProvider jwtTokenProvider;

  public JwtAuthorizationFilter(final JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
      final FilterChain filterChain) throws IOException, ServletException {
    final String authHeader = ((HttpServletRequest) servletRequest)
        .getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      final String authToken = authHeader.substring(7);
      final Authentication resultAuthentication = jwtTokenProvider.authenticate(authToken);
      SecurityContextHolder.getContext().setAuthentication(resultAuthentication);
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }
}
