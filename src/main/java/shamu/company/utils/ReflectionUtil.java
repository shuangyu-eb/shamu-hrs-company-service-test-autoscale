package shamu.company.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import shamu.company.common.exception.GeneralException;

public interface ReflectionUtil {

  static <T> T convertTo(final Object object, final Class<T> className) {
    T newInstance = null;
    try {
      newInstance = className.getDeclaredConstructor().newInstance();
    } catch (final InstantiationException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException e) {
      throw new GeneralException("Can not construct a new instance of " + className);
    }

    BeanUtils.copyProperties(object, newInstance);
    return newInstance;
  }

  static <T> List<T> convertTo(final List<?> objects, final Class<T> className) {
    return objects.stream()
        .map(object -> convertTo(object, className))
        .collect(Collectors.toList());
  }
}
