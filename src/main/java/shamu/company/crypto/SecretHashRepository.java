package shamu.company.crypto;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import shamu.company.common.exception.GeneralException;

@Repository
public class SecretHashRepository {

  private final JdbcTemplate secretJdbcTemplate;

  public SecretHashRepository(final JdbcTemplate secretJdbcTemplate) {
    this.secretJdbcTemplate = secretJdbcTemplate;
  }

  public String getCompanySecretByCompanyId(final String companyId) {

    final String sql = "SELECT secret_hash FROM company_secrets WHERE "
        + "company_id = unhex(?) LIMIT 1";
    String secret = "";
    try {
      secret = secretJdbcTemplate.queryForObject(sql, String.class, companyId);
    } catch (final EmptyResultDataAccessException e) {
      throw new GeneralException("No secret hash of this company.", e);
    }
    return secret;
  }
}
