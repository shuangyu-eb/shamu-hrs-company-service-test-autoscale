package shamu.company.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import shamu.company.utils.exception.DeserializeFailedException;
import shamu.company.utils.exception.SerializeFailedException;

public abstract class JsonUtil {

  private static final ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private JsonUtil() {}

  public static String formatToString(final Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (final JsonProcessingException e) {
      throw new SerializeFailedException("Error while serializing.");
    }
  }

  public static <T> T deserialize(final String objectString, final Class<T> className) {
    try {
      return objectMapper.readValue(objectString, className);
    } catch (final IOException e) {
      throw new DeserializeFailedException("Error while parsing JSON string in deserialize.");
    }
  }

  public static <T> T deserializeType(final String objectString, final TypeReference<T> type) {
    try {
      return objectMapper.readValue(objectString, type);
    } catch (final IOException e) {
      throw new DeserializeFailedException("Error while parsing JSON string in deserializeType.");
    }
  }
}
