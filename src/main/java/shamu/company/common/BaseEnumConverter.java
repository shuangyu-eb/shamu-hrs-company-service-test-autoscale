package shamu.company.common;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import javax.persistence.AttributeConverter;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import shamu.company.common.exception.GeneralException;
import shamu.company.common.exception.ResourceNotFoundException;

@Repository
public class BaseEnumConverter<X> implements AttributeConverter<X, Long> {

  private Class<X> xclazz;
  private Method valueMethod;
  private String tableName;


  private static EntityManager entityManager;

  private String persistSql = "select id from %s where name='%s' and deleted_at is null";
  private String loadSql = "select name from %s where id=%d and deleted_at is null";

  @Autowired
  public BaseEnumConverter(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @SuppressWarnings("unchecked")
  public BaseEnumConverter() {
    this.xclazz = (Class<X>) (((ParameterizedType) this.getClass().getGenericSuperclass())
        .getActualTypeArguments())[0];
    try {
      valueMethod = xclazz.getMethod("valueOf", String.class);
      tableName = xclazz.getAnnotation(ConverterTable.class).value();
    } catch (Exception e) {
      throw new GeneralException("can't get values method from " + xclazz);
    }


  }

  @Override
  public Long convertToDatabaseColumn(X attribute) {
    entityManager.clear();
    Query query = entityManager.createNativeQuery(
        String.format(persistSql, tableName, attribute.toString()));
    BigInteger i = (BigInteger) query.getSingleResult();

    return i.longValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public X convertToEntityAttribute(Long id) {
    if (id == null) {
      return null;
    }

    String name = (String) entityManager.createNativeQuery(String.format(loadSql, tableName, id))
        .getSingleResult();
    try {
      return (X) valueMethod.invoke(null, name);
    } catch (Exception e) {
      throw new ResourceNotFoundException("can't convertToEntityAttribute" + e.getMessage());
    }
  }
}