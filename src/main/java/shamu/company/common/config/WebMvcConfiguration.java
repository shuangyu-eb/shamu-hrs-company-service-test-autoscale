package shamu.company.common.config;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import shamu.company.crypto.CryptoValueFilter;
import shamu.company.hashids.Converter;
import shamu.company.hashids.HashidsFormatterFactory;
import shamu.company.s3.PreSignedValueFilter;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

  @Autowired
  PreSignedValueFilter preSignedValueFilter;

  @Autowired
  CryptoValueFilter cryptoValueFilter;

  @Bean
  @Override
  @Nullable
  public RequestMappingHandlerMapping requestMappingHandlerMapping() {
    final RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
    handlerMapping.setUseSuffixPatternMatch(false);
    return handlerMapping;
  }

  @Override
  protected void addFormatters(final FormatterRegistry registry) {
    super.addFormatters(registry);
    final HashidsFormatterFactory hashidsFormatterFactory = new HashidsFormatterFactory();
    registry.addFormatterForFieldAnnotation(hashidsFormatterFactory);
  }

  @Override
  protected void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
    final FastJsonHttpMessageConverter convert =
        new Converter(preSignedValueFilter, cryptoValueFilter);
    converters.add(convert);
  }

}
