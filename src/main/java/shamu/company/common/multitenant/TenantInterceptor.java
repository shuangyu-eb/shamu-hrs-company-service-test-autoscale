package shamu.company.common.multitenant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import shamu.company.common.service.TenantService;

@Component
public class TenantInterceptor extends HandlerInterceptorAdapter {

  private final JwtDecoder decoder;

  private final String customNamespace;

  private final TenantService tenantService;

  public TenantInterceptor(
      final JwtDecoder decoder,
      final @Value("${auth0.customNamespace}") String customNamespace,
      final TenantService tenantService) {
    this.decoder = decoder;
    this.customNamespace = customNamespace;
    this.tenantService = tenantService;
  }

  @Override
  public boolean preHandle(
      final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
    String bearerToken = request.getHeader("Authorization");

    if (StringUtils.isBlank(bearerToken)) {
      return true;
    }

    String companyId = request.getHeader("X-Mock-Company");

    bearerToken = bearerToken.replace("Bearer", "").replace(" ", "");

    if (StringUtils.isEmpty(companyId)) {
      companyId = getCompanyIdFromToken(bearerToken).toUpperCase();
    }

    if (StringUtils.isEmpty(companyId) || !tenantService.isCompanyExists(companyId)) {
      return false;
    }

    TenantContext.setCurrentTenant(companyId);
    return true;
  }

  @Override
  public void afterCompletion(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final Object handler,
      @Nullable final Exception ex) {
    TenantContext.clear();
  }

  private String getCompanyIdFromToken(final String token) {
    final Jwt jwt = decoder.decode(token);
    return jwt.getClaimAsString(String.format("%scompanyId", customNamespace));
  }
}
