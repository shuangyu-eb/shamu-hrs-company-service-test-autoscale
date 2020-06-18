package shamu.company.crypto;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Repository;
import shamu.company.crypto.exception.SecretHashNotFoundException;
import shamu.company.sentry.SentryLogger;

@Repository
public class SecretHashRepository {

  private static final SentryLogger log = new SentryLogger(SecretHashRepository.class);

  private final JdbcTemplate secretJdbcTemplate;

  public SecretHashRepository(final JdbcTemplate secretJdbcTemplate) {
    this.secretJdbcTemplate = secretJdbcTemplate;
  }

  public String getCompanySecretByCompanyId(final String companyId) {

    final String sql =
        "SELECT secret_hash FROM company_secrets WHERE " + "company_id = unhex(?) LIMIT 1";
    String secret = "";
    try {
      secret = secretJdbcTemplate.queryForObject(sql, String.class, companyId);
    } catch (final EmptyResultDataAccessException e) {
      throw new SecretHashNotFoundException("No secret hash of this company.", e);
    }
    return secret;
  }

  public void generateCompanySecretByCompanyId(final String companyId) {
    final String sql = "INSERT INTO company_secrets (company_id, secret_hash) VALUES (unhex(?), ?)";
    final String secretHash = BCrypt.hashpw(companyId, BCrypt.gensalt());

    try {
      secretJdbcTemplate.update(sql, companyId, secretHash);
    } catch (final DataAccessException e) {
      log.error("Failed to generate secret for company id " + companyId, e);
    }
  }
}
