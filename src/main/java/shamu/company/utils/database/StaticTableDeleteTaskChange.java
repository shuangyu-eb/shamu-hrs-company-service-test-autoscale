package shamu.company.utils.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
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
  public void execute(Database database) throws CustomChangeException {
    JdbcConnection databaseConnection = (JdbcConnection) database.getConnection();
    try {
      PreparedStatement preparedStatement =
          databaseConnection.prepareStatement(
              "DELETE FROM company." + tableName + " WHERE ref_id = ? and name = ? ");
      preparedStatement.setString(1, refId);
      preparedStatement.setString(2, value);
      preparedStatement.execute();
      preparedStatement.close();
    } catch (DatabaseException | SQLException e) {
      throw new GeneralException("Delete static table error: " + e.getMessage());
    }
  }

  @Override
  public String getConfirmationMessage() {
    return "Static Table " + this.tableName + " updates!";
  }

  @Override
  public void setUp() throws SetupException {

  }

  @Override
  public void setFileOpener(ResourceAccessor resourceAccessor) {

  }

  @Override
  public ValidationErrors validate(Database database) {
    return new ValidationErrors();
  }

}
