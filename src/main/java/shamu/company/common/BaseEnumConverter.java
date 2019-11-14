package shamu.company.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import javax.persistence.AttributeConverter;
import shamu.company.common.exception.GeneralException;
import shamu.company.common.exception.ResourceNotFoundException;

public class BaseEnumConverter<X> implements AttributeConverter<X, String> {

  private Method valueOfMethod;

  private Method valueMethod;

  @SuppressWarnings("unchecked")
  public BaseEnumConverter() {
    Class<X> xclazz = (Class<X>) (((ParameterizedType) this.getClass().getGenericSuperclass())
        .getActualTypeArguments())[0];
    try {
      valueOfMethod = xclazz.getMethod("valueOf", String.class);
      valueMethod = xclazz.getMethod("getValue");
    } catch (Exception e) {
      throw new GeneralException("can't get values method from " + xclazz);
    }
  }

  @Override
  public String convertToDatabaseColumn(X attribute) {
    if (attribute == null) {
      return null;
    }

    try {
      return (String) valueMethod.invoke(attribute);
    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
      throw new GeneralException(attribute.toString() + " invoke method getValue() failed");
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public X convertToEntityAttribute(String id) {
    if (id == null) {
      return null;
    }

    try {
      return (X) valueOfMethod.invoke(null, id);
    } catch (Exception e) {
      throw new ResourceNotFoundException("can't convertToEntityAttribute" + e.getMessage());
    }
  }
}
