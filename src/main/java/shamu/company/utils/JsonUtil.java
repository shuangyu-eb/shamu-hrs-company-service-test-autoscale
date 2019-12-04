package shamu.company.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import shamu.company.common.exception.ForbiddenException;

public abstract class JsonUtil {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private JsonUtil() {}

  public static String formatToString(final Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (final JsonProcessingException e) {
      throw new ForbiddenException("Error while serializing.", e);
    }
  }

  public static <T> T deserialize(final String objectString, final Class<T> className) {
    try {
      return objectMapper.readValue(objectString, className);
    } catch (final IOException e) {
      throw new ForbiddenException("Error while parse json string.", e);
    }
  }
}
