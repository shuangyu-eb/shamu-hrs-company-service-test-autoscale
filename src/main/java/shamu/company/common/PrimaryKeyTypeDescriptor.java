package shamu.company.common;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.BinaryType;
import org.hibernate.usertype.UserType;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

public class PrimaryKeyTypeDescriptor implements UserType {

  @Override
  public int[] sqlTypes() {
    return new int[]{
        BinaryType.INSTANCE.sqlType()
    };
  }

  @Override
  public Class returnedClass() {
    return String.class;
  }

  @Override
  public boolean equals(Object o, Object o1) {
    return Objects.equals(o, o1);
  }

  @Override
  public int hashCode(Object o) {
    return super.hashCode();
  }

  @Override
  public Object nullSafeGet(ResultSet resultSet, String[] strings,
      SharedSessionContractImplementor sharedSessionContractImplementor, Object o)
      throws SQLException {
    if (strings.length == 0) {
      return null;
    }

    byte[] bytes = resultSet.getBytes(strings[0]);
    if (bytes == null) {
      return null;
    }

    return UuidUtil.toHexString(bytes);
  }

  @Override
  public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i,
      SharedSessionContractImplementor sharedSessionContractImplementor) throws SQLException {
    byte[] bytes = o == null ? null : UuidUtil.toBytes((String) o);
    BinaryType.INSTANCE.set(preparedStatement, bytes, i, sharedSessionContractImplementor);
  }

  @Override
  public Object deepCopy(Object o) {
    String objectString = JsonUtil.formatToString(o);
    return JsonUtil.deserialize(objectString, o.getClass());
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Serializable disassemble(Object o) {
    return (Serializable) o;
  }

  @Override
  public Object assemble(Serializable serializable, Object o) {
    return serializable;
  }

  @Override
  public Object replace(Object original, Object target, Object owner) {
    return original;
  }
}
