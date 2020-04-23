package shamu.company.sentry;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentryLogger {

  private final Logger log;
  private final String className;

  public SentryLogger(final Class<?> logClass) {
    className = logClass.getName();
    log = LoggerFactory.getLogger(className);
  }

  public void debug(final String message) {
    final EventBuilder eventBuilder =
        new EventBuilder().withMessage(message).withLevel(Event.Level.DEBUG).withLogger(className);

    log.debug(message);
    Sentry.capture(eventBuilder);
  }

  public void debug(final String message, final Exception e) {
    final EventBuilder eventBuilder =
        new EventBuilder()
            .withMessage(message)
            .withLevel(Event.Level.DEBUG)
            .withLogger(className)
            .withSentryInterface(new ExceptionInterface(e));

    log.debug(message, e);
    Sentry.capture(eventBuilder);
  }

  public void info(final String message) {
    final EventBuilder eventBuilder =
        new EventBuilder().withMessage(message).withLevel(Event.Level.INFO).withLogger(className);

    log.info(message);
    Sentry.capture(eventBuilder);
  }

  public void info(final String message, final Exception e) {
    final EventBuilder eventBuilder =
        new EventBuilder()
            .withMessage(message)
            .withLevel(Event.Level.INFO)
            .withLogger(className)
            .withSentryInterface(new ExceptionInterface(e));

    log.info(message, e);
    Sentry.capture(eventBuilder);
  }

  public void warn(final String message) {
    final EventBuilder eventBuilder =
        new EventBuilder()
            .withMessage(message)
            .withLevel(Event.Level.WARNING)
            .withLogger(className);

    log.warn(message);
    Sentry.capture(eventBuilder);
  }

  public void warn(final String message, final Exception e) {
    final EventBuilder eventBuilder =
        new EventBuilder()
            .withMessage(message)
            .withLevel(Event.Level.WARNING)
            .withLogger(className)
            .withSentryInterface(new ExceptionInterface(e));

    log.warn(message, e);
    Sentry.capture(eventBuilder);
  }

  public void error(final String message) {
    final EventBuilder eventBuilder =
        new EventBuilder().withMessage(message).withLevel(Event.Level.ERROR).withLogger(className);

    log.error(message);
    Sentry.capture(eventBuilder);
  }

  public void error(final String message, final Exception e) {
    final EventBuilder eventBuilder =
        new EventBuilder()
            .withMessage(message)
            .withLevel(Event.Level.ERROR)
            .withLogger(className)
            .withSentryInterface(new ExceptionInterface(e));

    log.error(message, e);
    Sentry.capture(eventBuilder);
  }
}
