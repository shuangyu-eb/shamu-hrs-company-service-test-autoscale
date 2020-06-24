package shamu.company.common.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {
  @Bean
  public ErrorDecoder errorDecoder() {
    return new FeignErrorDecoder();
  }

  @Bean
  public RequestInterceptor requestInterceptor() {
    return new FeignAuthInterceptor();
  }
}
