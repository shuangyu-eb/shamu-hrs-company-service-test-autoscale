package shamu.company.common.config;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import shamu.company.hashids.Converter;
import shamu.company.hashids.HashidsFormatterFactory;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

  @Bean
  @Override
  @Nullable
  public RequestMappingHandlerMapping requestMappingHandlerMapping() {
    RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
    handlerMapping.setUseSuffixPatternMatch(false);
    return handlerMapping;
  }

  @Override
  protected void addFormatters(FormatterRegistry registry) {
    super.addFormatters(registry);
    HashidsFormatterFactory hashidsFormatterFactory = new HashidsFormatterFactory();
    registry.addFormatterForFieldAnnotation(hashidsFormatterFactory);
  }

  @Override
  protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    FastJsonHttpMessageConverter convert = new Converter();
    converters.add(convert);
  }
}
