package shamu.company.utils.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import lombok.Data;
import shamu.company.common.exception.GeneralException;

@Data
public class StaticTableDeleteTaskChange implements CustomTaskChange {

  private String tableName;

  private String refId;

  private String value;

  @Override
  public void execute(final Database database) {
    final JdbcConnection databaseConnection = (JdbcConnection) database.getConnection();
    try {
      final PreparedStatement preparedStatement =
          databaseConnection.prepareStatement(
              "DELETE FROM company." + tableName + " WHERE ref_id = ? and name = ? ");
      preparedStatement.setString(1, refId);
      preparedStatement.setString(2, value);
      preparedStatement.execute();
      preparedStatement.close();
    } catch (final DatabaseException | SQLException e) {
      throw new GeneralException("Delete static table error: " + e.getMessage());
    }
  }

  @Override
  public String getConfirmationMessage() {
    return "Static Table " + tableName + " updates!";
  }

  @Override
  public void setUp() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFileOpener(final ResourceAccessor resourceAccessor) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ValidationErrors validate(final Database database) {
    return new ValidationErrors();
  }

}
