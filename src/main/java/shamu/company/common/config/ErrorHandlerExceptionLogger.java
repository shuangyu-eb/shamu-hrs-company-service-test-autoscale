package shamu.company.common.config;

import lombok.extern.slf4j.Slf4j;
import me.alidg.errors.ExceptionLogger;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ErrorHandlerExceptionLogger implements ExceptionLogger {

  @Override
  public void log(Throwable exception) {
    log.error("Failed to process exception.", exception);
  }
}
