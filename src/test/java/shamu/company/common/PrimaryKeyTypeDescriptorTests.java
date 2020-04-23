package shamu.company.common;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PrimaryKeyTypeDescriptorTests {

  private final PrimaryKeyTypeDescriptor primaryKeyTypeDescriptor = new PrimaryKeyTypeDescriptor();

  @Test
  void testEquals() {
    Assertions.assertFalse(primaryKeyTypeDescriptor.equals("1", "2"));
  }

  @Test
  void testAssemble() {
    Assertions.assertNotNull(
        primaryKeyTypeDescriptor.assemble(
            new Serializable() {
              private static final long serialVersionUID = -8043411493710940055L;

              @Override
              public boolean equals(final Object obj) {
                return super.equals(obj);
              }
            },
            "1"));
  }

  @Test
  void testDisassemble() {
    Assertions.assertNotNull(primaryKeyTypeDescriptor.disassemble("1"));
  }

  @Test
  void testReplace() {
    Assertions.assertNotNull(primaryKeyTypeDescriptor.replace("1", "2", "3"));
  }

  @Nested
  class testNullSafeGet {

    ResultSet resultSet;
    SharedSessionContractImplementor sharedSessionContractImplementor;

    @BeforeEach
    void init() {
      resultSet = Mockito.mock(ResultSet.class);
      sharedSessionContractImplementor = Mockito.mock(SharedSessionContractImplementor.class);
    }

    @Test
    void whenStringLengthZero_thenShouldReturnNull() throws SQLException {
      final String[] arr = {};
      Assertions.assertNull(
          primaryKeyTypeDescriptor.nullSafeGet(
              resultSet, arr, sharedSessionContractImplementor, "1"));
    }

    @Test
    void whenResultSetNull_thenShouldReturnNull() throws SQLException {
      final String[] arr = {"1"};
      Mockito.when(resultSet.getBytes(Mockito.anyString())).thenReturn(null);
      Assertions.assertNull(
          primaryKeyTypeDescriptor.nullSafeGet(
              resultSet, arr, sharedSessionContractImplementor, "1"));
    }
  }
}
