package shamu.company.utils;

import java.math.BigInteger;
import java.util.Arrays;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import shamu.company.common.ConverterTable;
import shamu.company.timeoff.entity.TimeOffRequestApprovalStatus;

@Component
public class EnumRunner implements CommandLineRunner {

  private final EntityManager entityManager;

  private final String sql = "SELECT id FROM %s WHERE name='%s' ";

  @Autowired
  public EnumRunner(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public void run(String... args) {
    this.setTimeOffRequestApprovalStatusId();
  }


  private void setTimeOffRequestApprovalStatusId() {
    Class<TimeOffRequestApprovalStatus> clazz = TimeOffRequestApprovalStatus.class;
    String tableName = clazz.getAnnotation(ConverterTable.class).value();

    Arrays.stream(TimeOffRequestApprovalStatus.values()).forEach(status -> {
      Object id = this.entityManager
          .createNativeQuery(String.format(sql, tableName, status.name())).getSingleResult();

      status.setValue(((BigInteger) id).longValue());
    });
  }
}
