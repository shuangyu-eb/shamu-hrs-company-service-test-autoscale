package shamu.company.common.config;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoderJwkSupport;
import shamu.company.helpers.auth0.Auth0Config;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.server.dto.AuthUser;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final DefaultAuthenticationEntryPoint defaultAuthenticationEntryPoint;

  private final Auth0Config auth0Config;

  private final AuthUserCacheManager authUserCacheManager;

  @Autowired
  public WebSecurityConfiguration(
      final DefaultAuthenticationEntryPoint defaultAuthenticationEntryPoint,
      final Auth0Config auth0Config,
      final AuthUserCacheManager authUserCacheManager) {
    this.defaultAuthenticationEntryPoint = defaultAuthenticationEntryPoint;
    this.auth0Config = auth0Config;
    this.authUserCacheManager = authUserCacheManager;
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .cors()
        .disable()
        .httpBasic()
        .disable()
        .formLogin()
        .disable()
        .authorizeRequests()
        .antMatchers("/actuator/**")
        .permitAll()
        .antMatchers(
            "/company/user/verify",
            "/company/paid-holidays",
            "/company/user/check/**",
            "/company/users/email-check/**",
            "/company/account/password",
            "/company/account/password/**",
            "/company/users/password-reset",
            "/company/users/password-reset/**",
            "/company/users",
            "/company/account/change-work-email/*",
            "/company/company-sizes",
            "/company/account/*/verification-email",
            "/company/account/unlock",
            "/company/account/email/*",
            "/company/emails/status")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(defaultAuthenticationEntryPoint)
        .and()
        .oauth2ResourceServer()
        .jwt()
        .jwtAuthenticationConverter(new AuthenticationConverter(auth0Config.getCustomNamespace()));
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
  }

  @Bean
  @ConditionalOnMissingBean
  public JwtDecoder jwtDecoder() {
    return new NimbusJwtDecoderJwkSupport(auth0Config.getJwks());
  }

  class AuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final String customNamespace;

    AuthenticationConverter(final String customNamespace) {
      this.customNamespace = customNamespace;
    }

    @Override
    public AbstractAuthenticationToken convert(final Jwt jwt) {

      final AuthUser authUser = authUserCacheManager.getCachedUser(jwt.getTokenValue());

      final List<String> authorities =
          authUser == null ? Collections.emptyList() : authUser.getPermissions();

      final List<SimpleGrantedAuthority> grantedAuthorities =
          authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

      final String id = jwt.getClaimAsString(String.format("%sid", customNamespace));

      return new DefaultJwtAuthenticationToken(jwt, id, grantedAuthorities, authUser);
    }
  }
}
