package shamu.company.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PrimaryKeyTypeDescriptorTests {

  private final PrimaryKeyTypeDescriptor primaryKeyTypeDescriptor = new PrimaryKeyTypeDescriptor();

  @Test
  void testEquals() {
    assertThat(primaryKeyTypeDescriptor.equals("1", "2")).isFalse();
  }

  @Test
  void testAssemble() {
    assertThat(
            primaryKeyTypeDescriptor.assemble(
                new Serializable() {
                  private static final long serialVersionUID = -8043411493710940055L;

                  @Override
                  public boolean equals(final Object obj) {
                    return super.equals(obj);
                  }
                },
                "1"))
        .isNotNull();
  }

  @Test
  void testDisassemble() {
    assertThat(primaryKeyTypeDescriptor.disassemble("1")).isNotNull();
  }

  @Test
  void testReplace() {
    assertThat(primaryKeyTypeDescriptor.replace("1", "2", "3")).isNotNull();
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
      assertThat(
              primaryKeyTypeDescriptor.nullSafeGet(
                  resultSet, arr, sharedSessionContractImplementor, "1"))
          .isNull();
    }

    @Test
    void whenResultSetNull_thenShouldReturnNull() throws SQLException {
      final String[] arr = {"1"};
      Mockito.when(resultSet.getBytes(Mockito.anyString())).thenReturn(null);
      assertThat(
              primaryKeyTypeDescriptor.nullSafeGet(
                  resultSet, arr, sharedSessionContractImplementor, "1"))
          .isNull();
    }
  }
}
