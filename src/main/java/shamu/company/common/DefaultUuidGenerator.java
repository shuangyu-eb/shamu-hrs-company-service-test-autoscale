package shamu.company.common;

import java.io.Serializable;
import java.util.UUID;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class DefaultUuidGenerator implements IdentifierGenerator {

  public Serializable generate(SharedSessionContractImplementor session, Object object) {
    return UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
  }
}
