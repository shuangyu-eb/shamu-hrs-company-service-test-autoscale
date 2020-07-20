package shamu.company.common.config;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import shamu.company.employee.interceptor.UserInvitationRateLimitingInterceptor;
import shamu.company.timeoff.interceptor.TimeOffRequestRateLimitingInterceptor;
import shamu.company.common.multitenant.TenantInterceptor;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

  private final UserInvitationRateLimitingInterceptor userInvitingRateLimitInterceptor;

  private final TimeOffRequestRateLimitingInterceptor timeOffRequestRateLimitingInterceptor;

  public WebMvcConfiguration(
      @Lazy final TenantInterceptor tenantInterceptor
      @Lazy final UserInvitationRateLimitingInterceptor userInvitingRateLimitInterceptor,
      @Lazy final TimeOffRequestRateLimitingInterceptor timeOffRequestRateLimitingInterceptor) {
    this.tenantInterceptor = tenantInterceptor;
    this.userInvitingRateLimitInterceptor = userInvitingRateLimitInterceptor;
    this.timeOffRequestRateLimitingInterceptor = timeOffRequestRateLimitingInterceptor;
  }

  @Bean
  @Override
  @Nullable
  public RequestMappingHandlerMapping requestMappingHandlerMapping() {
    final RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
    handlerMapping.setUseSuffixPatternMatch(false);
    return handlerMapping;
  }

  @Override
  public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
    configurer.favorPathExtension(false);
  }

  @Override
  protected void extendMessageConverters(final List<HttpMessageConverter<?>> converters) {
    converters.forEach(
        httpMessageConverter -> {
          if (httpMessageConverter instanceof StringHttpMessageConverter) {
            ((StringHttpMessageConverter) httpMessageConverter)
                .setDefaultCharset(StandardCharsets.UTF_8);
          }
        });
  }

  @Override
  protected void addInterceptors(final InterceptorRegistry registry) {
    registry.addInterceptor(tenantInterceptor);
    registry.addInterceptor(userInvitingRateLimitInterceptor).addPathPatterns("/company/employees");
    registry
        .addInterceptor(timeOffRequestRateLimitingInterceptor)
        .addPathPatterns("/company/users/*/time-off-requests")
        .addPathPatterns("/company/users/*/time-off-requests/approved");
  }
}
