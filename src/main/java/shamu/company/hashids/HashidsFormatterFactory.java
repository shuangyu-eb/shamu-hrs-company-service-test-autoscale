package shamu.company.hashids;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.context.support.EmbeddedValueResolutionSupport;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Formatter;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

public class HashidsFormatterFactory extends EmbeddedValueResolutionSupport implements
    AnnotationFormatterFactory<HashidsFormat> {

  private static final Set<Class<?>> FIELD_TYPES;

  static {
    Set<Class<?>> fieldTypes = new HashSet<>(2);
    fieldTypes.add(Long.class);
    fieldTypes.add(long.class);
    FIELD_TYPES = Collections.unmodifiableSet(fieldTypes);
  }

  @Override
  public Set<Class<?>> getFieldTypes() {
    return FIELD_TYPES;
  }

  @Override
  public Printer<?> getPrinter(HashidsFormat hashidsFormat, Class<?> clazz) {
    return getFormatter();
  }

  @Override
  public Parser<?> getParser(HashidsFormat hashidsFormat, Class<?> clazz) {
    return getFormatter();
  }

  private Formatter<Long> getFormatter() {
    return new HashidsFormatter();
  }
}
