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
import shamu.company.s3.PreSignedValueFilter;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

  private final PreSignedValueFilter preSignedValueFilter;

  private final CryptoValueFilter cryptoValueFilter;

  @Autowired
  public WebMvcConfiguration(final PreSignedValueFilter preSignedValueFilter,
      final CryptoValueFilter cryptoValueFilter) {
    this.preSignedValueFilter = preSignedValueFilter;
    this.cryptoValueFilter = cryptoValueFilter;
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
  protected void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
    final FastJsonHttpMessageConverter convert =
        new HttpJsonMessageConverter(preSignedValueFilter, cryptoValueFilter);
    converters.add(convert);
  }

  @Override
  protected void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringFieldConditionalConverter());
  }
}
