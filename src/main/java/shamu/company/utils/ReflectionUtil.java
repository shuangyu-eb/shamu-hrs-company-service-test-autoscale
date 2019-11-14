package shamu.company.utils;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import shamu.company.common.exception.GeneralException;

public class ReflectionUtil {

  public static <T> T convertTo(Object object, Class<T> className) {
    T newInstance = null;
    try {
      newInstance = className.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new GeneralException("Can not construct a new instance of " + className);
    }

    BeanUtils.copyProperties(object, newInstance);
    return newInstance;
  }

  public static <T> List<T> convertTo(List<?> objects, Class<T> className) {
    return objects.stream().map(object -> convertTo(object, className))
        .collect(Collectors.toList());
  }
}
