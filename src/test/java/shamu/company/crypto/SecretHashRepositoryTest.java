package shamu.company.crypto;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import shamu.company.common.exception.GeneralException;

class SecretHashRepositoryTest {

  @InjectMocks SecretHashRepository secretHashRepository;

  @Mock JdbcTemplate secretJdbcTemplate;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class getCompanySecretByCompanyId {
    final String sql =
        "SELECT secret_hash FROM company_secrets WHERE " + "company_id = unhex(?) LIMIT 1";
    String secret = "";
    String companyId = "1";

    @Test
    void whenGetSecret_thenShouldSuccess() {
      Mockito.when(secretJdbcTemplate.queryForObject(sql, String.class, companyId))
          .thenReturn(secret);
      secretHashRepository.getCompanySecretByCompanyId(companyId);
      Mockito.verify(secretJdbcTemplate, Mockito.times(1))
          .queryForObject(sql, String.class, companyId);
    }

    @Test
    void whenSecretHashIsEmpty_thenShouldThrow() {
      Mockito.when(secretJdbcTemplate.queryForObject(sql, String.class, companyId))
          .thenThrow(EmptyResultDataAccessException.class);
      assertThatExceptionOfType(GeneralException.class)
          .isThrownBy(() -> secretHashRepository.getCompanySecretByCompanyId(companyId));
    }
  }

  @Nested
  class generateCompanySecretByCompanyId {
    String companyId = "1";

    @Test
    void whenUpdateSuccess_thenShouldSuccess() {
      secretHashRepository.generateCompanySecretByCompanyId(companyId);
      Mockito.verify(secretJdbcTemplate, Mockito.times(1))
          .update(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }
  }
}
