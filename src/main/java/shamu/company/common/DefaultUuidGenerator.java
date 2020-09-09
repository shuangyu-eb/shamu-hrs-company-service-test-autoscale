package shamu.company.common;

import java.io.Serializable;
import org.apache.commons.lang.StringUtils;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import shamu.company.common.entity.BaseEntity;
import shamu.company.utils.UuidUtil;

public class DefaultUuidGenerator implements IdentifierGenerator {

  @Override
  public Serializable generate(
      final SharedSessionContractImplementor session, final Object object) {
    if (object instanceof BaseEntity) {
      final BaseEntity entity = (BaseEntity) object;
      if (StringUtils.isNotEmpty(entity.getId())) {
        return entity.getId();
      }
    }
    return UuidUtil.getUuidString().toUpperCase();
  }
}
