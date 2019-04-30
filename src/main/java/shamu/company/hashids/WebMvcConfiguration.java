package shamu.company.hashids;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

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
