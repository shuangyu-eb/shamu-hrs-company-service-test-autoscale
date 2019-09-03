package shamu.company.common.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import shamu.company.user.entity.User;
import shamu.company.user.repository.UserRepository;

@Component
@Order
public class SecurityContextFilter extends GenericFilterBean {

  private final UserRepository userRepository;

  public SecurityContextFilter(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    Authentication authentication = SecurityContextHolder
        .getContext().getAuthentication();

    if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
      DefaultJwtAuthenticationToken authenticationToken =
          (DefaultJwtAuthenticationToken) authentication;
      String userId = authenticationToken.getId();
      User user = userRepository.findByUserId(userId);
      SecurityHolder.getCurrentUser().set(user);
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }
}