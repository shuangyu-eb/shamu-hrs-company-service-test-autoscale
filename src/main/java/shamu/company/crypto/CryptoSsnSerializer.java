package shamu.company.crypto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import shamu.company.common.exception.GeneralException;
import shamu.company.utils.AnnotationUtil;

public class CryptoSsnSerializer extends JsonSerializer<String> {

  private Encryptor encryptor;

  public CryptoSsnSerializer() {
    super();
  }

  @Autowired
  public CryptoSsnSerializer(final Encryptor encryptor) {
    this.encryptor = encryptor;
  }

  @Override
  public void serialize(
      final String value, final JsonGenerator jsonGenerator, final SerializerProvider serializers)
      throws IOException {
    final Object currentObject = jsonGenerator.getOutputContext().getCurrentValue();
    final String fieldName = jsonGenerator.getOutputContext().getCurrentName();
    if (Strings.isNotBlank(value)) {
      final Crypto crypto =
          AnnotationUtil.getFieldAnnotation(currentObject, fieldName, Crypto.class)
              .orElseThrow(() -> new GeneralException("The field has no annotation"));
      final String id =
          (String)
              AnnotationUtil.getFieldValue(currentObject, crypto.field())
                  .orElseThrow(() -> new GeneralException("The field was not found"));
      final Class aClass = crypto.targetType();
      jsonGenerator.writeString(encryptor.decrypt(id, aClass, value));
    } else {
      jsonGenerator.writeNull();
    }
  }
}
