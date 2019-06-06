package shamu.company.common;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import javax.persistence.AttributeConverter;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shamu.company.common.exception.GeneralException;
import shamu.company.common.exception.ResourceNotFoundException;

@Component
public class BaseEnumConverter<X> implements AttributeConverter<X, Long> {

  private static EntityManager entityManager;
  private Class<X> xclazz;
  private Method valueMethod;
  private String tableName;
  private String persistSql = "select id from %s where name='%s' and deleted_at is null";
  private String loadSql = "select name from %s where id=%d and deleted_at is null";

  @Autowired
  public BaseEnumConverter(EntityManagerFactory entityManagerFactory) {
    this.entityManager = entityManagerFactory.createEntityManager();
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

  private void checkEntityManagerOpen() {
    if (!this.entityManager.isOpen()) {
      this.entityManager = this.entityManager.getEntityManagerFactory().createEntityManager();
    }
  }

  @Override
  public Long convertToDatabaseColumn(X attribute) {
    if (attribute == null) {
      return null;
    }

    this.checkEntityManagerOpen();
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
    this.checkEntityManagerOpen();
    String name = (String) entityManager.createNativeQuery(String.format(loadSql, tableName, id))
        .getSingleResult();
    try {
      return (X) valueMethod.invoke(null, name);
    } catch (Exception e) {
      throw new ResourceNotFoundException("can't convertToEntityAttribute" + e.getMessage());
    }
  }
}