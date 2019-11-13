package shamu.company.common.repository;

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

  public String getCompanySecretByCompanyId(final Long companyId) {
    final String sql = "SELECT secret_hash from company_secrets WHERE company_id = ?";
    String secret = "";
    try {
      secret = secretJdbcTemplate.queryForObject(sql, String.class, companyId);
    } catch (final EmptyResultDataAccessException e) {
      throw new GeneralException("No secret hash of this company.");
    }
    return secret;
  }
}
