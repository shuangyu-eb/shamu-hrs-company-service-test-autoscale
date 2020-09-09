package shamu.company.common.repository;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.NoResultException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import shamu.company.common.config.DataSourceConfig;
import shamu.company.common.entity.Tenant;
import shamu.company.common.exception.errormapping.LiquibaseExecuteFailedException;
import shamu.company.helpers.DatabaseSessionHelper;
import shamu.company.utils.UuidUtil;

@Repository
@Slf4j
public class TenantRepository {

  private final DatabaseSessionHelper sessionHelper;

  private final DataSourceConfig dataSourceConfig;

  private final DataSource companyDataSource;

  public TenantRepository(
      final DatabaseSessionHelper sessionHelper,
      final DataSourceConfig dataSourceConfig,
      final DataSource companyDataSource) {
    this.sessionHelper = sessionHelper;
    this.dataSourceConfig = dataSourceConfig;
    this.companyDataSource = companyDataSource;
  }

  public void save(final Tenant tenant) {
    final Optional<Tenant> optional = findByCompanyId(tenant.getCompanyId());
    tenant.setId(optional.orElse(new Tenant()).getId());
    try (final Session session = sessionHelper.getDefaultSession()) {
      final Transaction transaction = session.getTransaction();
      transaction.begin();
      session.saveOrUpdate(tenant);
      transaction.commit();
    }
  }

  public List<Tenant> findAll() {
    try (final Session session = sessionHelper.getDefaultSession()) {
      final Query<Tenant> query = session.createNativeQuery("SELECT * FROM tenants", Tenant.class);
      return query.getResultList();
    }
  }

  public Optional<Tenant> findByCompanyId(final String id) {
    try (final Session session = sessionHelper.getDefaultSession()) {
      final Query<Tenant> query =
          session.createNativeQuery(
              "SELECT * FROM tenants WHERE company_id = unhex(?1)", Tenant.class);
      query.setParameter(1, id);
      Tenant tenant;
      try {
        tenant = query.getSingleResult();
      } catch (final NoResultException e) {
        tenant = null;
      }
      return Optional.ofNullable(tenant);
    }
  }

  public List<Tenant> findAllByCompanyId(final List<String> ids) {
    try (final Session session = sessionHelper.getDefaultSession()) {

      final Query<Tenant> query =
          session.createNativeQuery("SELECT * FROM tenants WHERE company_id IN ?1", Tenant.class);
      query.setParameter(1, ids.stream().map(UuidUtil::toHexString).collect(Collectors.toList()));
      return query.getResultList();
    }
  }

  public Boolean isCompanyExists(final String id) {
    try (final Session session = sessionHelper.getDefaultSession()) {
      final Query query =
          session.createNativeQuery("SELECT COUNT(1) FROM tenants WHERE company_id = unhex(?1)");
      query.setParameter(1, id);
      final BigInteger result = (BigInteger) query.getSingleResult();
      return result.intValue() == 1;
    }
  }

  public Boolean existsByName(final String companyName) {
    try (final Session session = sessionHelper.getDefaultSession()) {
      final Query query = session.createNativeQuery("SELECT count(1) FROM tenants WHERE name = ?1");
      query.setParameter(1, companyName);
      final BigInteger result = (BigInteger) query.getSingleResult();
      return result.intValue() != 0;
    }
  }

  public Optional<Tenant> findTenantByUserEmailWork(final String emailWork) {
    final String sql =
        "select count(1) from users u "
            + "left join user_contact_information uc on u.user_contact_information_id = uc.id "
            + "where uc.email_work = ?1 "
            + "and (u.deactivated_at is null or (u.deactivated_at is not null "
            + "and u.deactivated_at > current_timestamp))";
    final List<Tenant> tenants = findAll();
    for (final Tenant tenant : tenants) {
      try (final Session session = sessionHelper.getSessionByCompanyId(tenant.getCompanyId())) {
        final Query query = session.createNativeQuery(sql);
        query.setParameter(1, emailWork);
        final BigInteger result = (BigInteger) query.getSingleResult();
        if (result.intValue() == 1) {
          return Optional.of(tenant);
        }
      }
    }
    return Optional.empty();
  }

  public void delete(final String companyId) {
    try (final Session session = sessionHelper.getDefaultSession()) {
      final Query<Tenant> query =
          session.createNativeQuery(
              "SELECT * FROM tenants WHERE company_id = unhex(?1)", Tenant.class);
      query.setParameter(1, companyId);
      try {
        final Tenant tenant = query.getSingleResult();
        final Transaction transaction = session.beginTransaction();
        session.delete(tenant);
        transaction.commit();
      } catch (final NoResultException e) {
        log.error(String.format("Tenant with id: %s is not exists.", companyId), e);
      } finally{
        deleteSchema(companyId);
      }
    }
  }

  private void deleteSchema(final String companyId) {
    final String schema = dataSourceConfig.getSchemaByCompanyId(companyId);
    final String sql = "DROP DATABASE " + schema;
    try (final Connection connection = companyDataSource.getConnection()) {
      try (final Statement statement = connection.createStatement()) {
        statement.execute(sql);
      }
    } catch (final SQLException e) {
      throw new LiquibaseExecuteFailedException("Error while dropping database", e);
    }
  }
}
