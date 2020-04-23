package shamu.company.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

  @Bean
  @Override
  @Nullable
  public RequestMappingHandlerMapping requestMappingHandlerMapping() {
    final RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
    handlerMapping.setUseSuffixPatternMatch(false);
    return handlerMapping;
  }

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.favorPathExtension(false);
  }
}
