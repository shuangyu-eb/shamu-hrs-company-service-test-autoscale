package shamu.company.common.multitenant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import shamu.company.authorization.Permission.Name;
import shamu.company.common.service.TenantService;

@Component
@Slf4j
public class TenantInterceptor extends HandlerInterceptorAdapter {

  private final JwtDecoder decoder;

  private final String customNamespace;

  private final TenantService tenantService;

  private static final String AUTH_HEADER = "Authorization";

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
    final String bearerToken = request.getHeader(AUTH_HEADER);
    if (StringUtils.isBlank(bearerToken)) {
      return true;
    }

    final String companyId = getCompanyIdFromRequest(request);
    if (StringUtils.isEmpty(companyId) || !tenantService.isCompanyExists(companyId)) {
      return false;
    }

    TenantContext.setCurrentTenant(companyId);
    return true;
  }

  private String getCompanyIdFromRequest(final HttpServletRequest request) {
    final String companyId = getCompanyIdForSuperAdmin(request);
    if (StringUtils.isNotEmpty(companyId)) {
      return companyId;
    }
    final String token = getTokenFromRequest(request);
    return getCompanyIdFromToken(token);
  }

  private String getCompanyIdForSuperAdmin(final HttpServletRequest request) {
    final String companyId = request.getHeader("X-Mock-Company");
    final String token = getTokenFromRequest(request);
    if (StringUtils.isNotEmpty(companyId) && hasSuperAdminPermission(token)) {
      return companyId;
    }
    return "";
  }

  private String getTokenFromRequest(final HttpServletRequest request) {
    final String bearerToken = request.getHeader(AUTH_HEADER);
    return bearerToken.replace("Bearer", "").replace(" ", "");
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

  private boolean hasSuperAdminPermission(final String token) {
    final Jwt jwt = decoder.decode(token);
    final JSONArray jsonArray = (JSONArray) jwt.getClaims().get("permissions");
    return jsonArray.stream()
        .anyMatch(object -> Name.SUPER_PERMISSION.name().equals(object.toString()));
  }
}
