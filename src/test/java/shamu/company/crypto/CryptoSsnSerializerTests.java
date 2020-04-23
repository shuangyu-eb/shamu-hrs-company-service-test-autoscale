package shamu.company.crypto;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;

class CryptoSsnSerializerTests {
  final String value = "a";
  @InjectMocks CryptoSsnSerializer cryptoSsnSerializer;
  ObjectMapper mapper;
  ByteArrayOutputStream bos;
  JsonGenerator jsonGenerator;
  SerializerProvider serializers;
  @Mock private Encryptor encryptor;

  @BeforeEach
  void init() throws IOException {
    MockitoAnnotations.initMocks(this);
    bos = new ByteArrayOutputStream();
    mapper = new ObjectMapper();
    serializers =
        new SerializerProvider() {
          @Override
          public WritableObjectId findObjectId(Object o, ObjectIdGenerator<?> objectIdGenerator) {
            return null;
          }

          @Override
          public JsonSerializer<Object> serializerInstance(Annotated annotated, Object o) {
            return null;
          }

          @Override
          public Object includeFilterInstance(
              BeanPropertyDefinition beanPropertyDefinition, Class<?> aClass) {
            return null;
          }

          @Override
          public boolean includeFilterSuppressNulls(Object o) {
            return false;
          }
        };
    jsonGenerator = mapper.getJsonFactory().createJsonGenerator(new PrintWriter(bos));
    jsonGenerator.setCurrentValue(UserPersonalInformation.class);
  }

  @Nested
  class serialize {

    @Test
    void whenValueIsBlank_thenShouldSuccess() throws IOException {
      cryptoSsnSerializer.serialize("", jsonGenerator, serializers);
      Mockito.verify(encryptor, Mockito.times(0)).decrypt("", User.class, value);
    }
  }
}
