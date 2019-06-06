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

  public JwtAuthorizationFilter(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    String authHeader = ((HttpServletRequest) servletRequest).getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String authToken = authHeader.substring(7);
      Authentication resultAuthentication = jwtTokenProvider.authenticate(authToken);
      SecurityContextHolder.getContext().setAuthentication(resultAuthentication);
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }
}
