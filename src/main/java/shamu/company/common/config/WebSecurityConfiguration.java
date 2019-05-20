package shamu.company.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private final JwtAuthorizationFilter jwtAuthorizationFilter;

  private final DefaultAuthenticationEntryPoint defaultAuthenticationEntryPoint;

  @Autowired
  public WebSecurityConfiguration(JwtAuthorizationFilter jwtAuthorizationFilter,
      DefaultAuthenticationEntryPoint defaultAuthenticationEntryPoint) {
    this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    this.defaultAuthenticationEntryPoint = defaultAuthenticationEntryPoint;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .cors().disable()
        .httpBasic().disable()
        .formLogin().disable()
        .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeRequests()
        .antMatchers("/actuator/**").permitAll()
        .antMatchers(
            "/company/user/verify",
            "/company/user/sign-up/email",
            "/company/paid-holidays",
            "/company/user/check/**",
            "/company/account/password",
            "/company/account/password/**",
            "/company/user/sign-up/email",
            "/company/user/password/reset/**"
        ).permitAll()
        .anyRequest().authenticated()
        .and()
        .exceptionHandling().authenticationEntryPoint(defaultAuthenticationEntryPoint);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
  }
}
