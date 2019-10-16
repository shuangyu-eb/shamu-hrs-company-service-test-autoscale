package shamu.company.common.config;

import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.spring.SentryExceptionResolver;
import io.sentry.spring.SentryServletContextInitializer;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@ConditionalOnProperty(value = "sentry.enable")
public class SentryConfiguration {

  @Value("${sentry.dsn}")
  String dsn;

  @Value("${sentry.env}")
  String environment;

  @Value("${sentry.release}")
  String release;

  @PostConstruct
  void init() {
    final SentryClient sentryClient = Sentry.init(dsn);
    sentryClient.setEnvironment(environment);
    sentryClient.setRelease(release);
  }

  @Bean
  @ConditionalOnMissingBean(SentryExceptionResolver.class)
  public HandlerExceptionResolver sentryExceptionResolver() {
    return new SentryExceptionResolver();
  }

  @Bean
  @ConditionalOnMissingBean(SentryServletContextInitializer.class)
  public ServletContextInitializer sentryServletContextInitializer() {
    return new SentryServletContextInitializer();
  }
}
