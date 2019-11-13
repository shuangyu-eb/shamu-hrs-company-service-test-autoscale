package shamu.company.crypto;

import com.alibaba.fastjson.serializer.ValueFilter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shamu.company.utils.AnnotationUtil;

@Component
public class CryptoValueFilter implements ValueFilter {

  private final Encryptor encryptor;

  @Autowired
  public CryptoValueFilter(final Encryptor encryptor) {
    this.encryptor = encryptor;
  }


  @Override
  public Object process(final Object object, final String name, final Object value) {
    if (object != null && value instanceof String
        && AnnotationUtil.fieldHasAnnotation(object.getClass(), name, Crypto.class)
        && !Strings.isBlank((String) value)) {
      final Crypto crypto = AnnotationUtil
          .getFieldAnnotation(object, name, Crypto.class);
      final Long id = (Long) AnnotationUtil.getFieldValue(object, crypto.field());
      final Class aClass = crypto.targetType();

      return encryptor.decrypt(id, aClass, (String) value);
    }
    return value;
  }
}
