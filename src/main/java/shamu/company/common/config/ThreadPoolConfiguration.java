package shamu.company.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ThreadPoolConfiguration {

  @Bean
  public ThreadPoolTaskExecutor getTaskExecutor() {
    ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setThreadNamePrefix("TaskExecutor");
    threadPoolTaskExecutor.setCorePoolSize(5);
    threadPoolTaskExecutor.setMaxPoolSize(10);
    return threadPoolTaskExecutor;
  }

  @Bean
  @Primary
  public ThreadPoolTaskScheduler getTaskScheduler() {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(10);
    threadPoolTaskScheduler.setThreadNamePrefix("TaskScheduler");
    return threadPoolTaskScheduler;
  }
}
