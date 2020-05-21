package shamu.company.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import javax.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;
import shamu.company.common.exception.GeneralException;
import shamu.company.common.exception.ResourceNotFoundException;

@Slf4j
public class BaseEnumConverter<X> implements AttributeConverter<X, String> {

  private final Method valueOfMethod;

  private final Method valueMethod;

  @SuppressWarnings("unchecked")
  public BaseEnumConverter() {
    final Class<X> xclazz =
        (Class<X>)
            (((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments())[0];
    try {
      valueOfMethod = xclazz.getMethod("valueOf", String.class);
      valueMethod = xclazz.getMethod("getValue");
    } catch (final Exception e) {
      throw new GeneralException("can't get values method from " + xclazz);
    }
  }

  @Override
  public String convertToDatabaseColumn(final X attribute) {
    if (attribute == null) {
      return null;
    }

    try {
      return (String) valueMethod.invoke(attribute);
    } catch (final IllegalAccessException | InvocationTargetException e) {
      log.error("Caught an exception when invoking method getValue(): " + e.getMessage());
      throw new GeneralException(attribute.toString() + " invoke method getValue() failed");
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public X convertToEntityAttribute(final String id) {
    if (id == null) {
      return null;
    }

    try {
      return (X) valueOfMethod.invoke(null, id);
    } catch (final Exception e) {
      throw new ResourceNotFoundException(
          String.format("ConvertToEntityAttribute with id %s not found!", id),
          id,
          "convertToEntityAttribute");
    }
  }
}
