package shamu.company.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import javax.persistence.Tuple;
import org.springframework.util.ReflectionUtils;
import shamu.company.common.exception.ForbiddenException;

public class TupleUtil {
  private TupleUtil() {}

  public static <T> T convertTo(Tuple tuple, Class<T> t) {
    try {
      T result = t.getDeclaredConstructor().newInstance();
      tuple.getElements().forEach(property -> {
        Field fieldResult = ReflectionUtils.findField(t, property.getAlias());
        if (fieldResult != null) {
          ReflectionUtils.makeAccessible(fieldResult);
          Object resultValue = tuple.get(property.getAlias());
          if (resultValue != null && property.getJavaType().isAssignableFrom(BigInteger.class)) {
            resultValue = ((BigInteger) resultValue).longValue();
          }
          ReflectionUtils.setField(fieldResult, result, resultValue);
        }
      });
      return result;
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new ForbiddenException(e.getMessage(), e);
    } catch (NoSuchMethodException e) {
      throw new ForbiddenException("No arg constructor not found in class " + t.getName());
    }
  }
}
