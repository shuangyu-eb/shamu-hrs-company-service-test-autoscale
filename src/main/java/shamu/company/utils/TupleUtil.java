package shamu.company.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import javax.persistence.Tuple;
import org.springframework.util.ReflectionUtils;
import shamu.company.common.exception.ForbiddenException;

public interface TupleUtil {

  static <T> T convertTo(final Tuple tuple, final Class<T> t) {
    try {
      final T result = t.getDeclaredConstructor().newInstance();
      tuple
          .getElements()
          .forEach(
              property -> {
                final Field fieldResult = ReflectionUtils.findField(t, property.getAlias());
                if (fieldResult != null) {
                  ReflectionUtils.makeAccessible(fieldResult);
                  Object resultValue = tuple.get(property.getAlias());

                  if (resultValue != null
                      && resultValue.getClass().isAssignableFrom(byte[].class)) {
                    resultValue = UuidUtil.toHexString((byte[]) resultValue);
                  }

                  ReflectionUtils.setField(fieldResult, result, resultValue);
                }
              });
      return result;
    } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new ForbiddenException(e.getMessage(), e);
    } catch (final NoSuchMethodException e) {
      throw new ForbiddenException("No arg constructor not found in class " + t.getName());
    }
  }
}
