package shamu.company.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import shamu.company.common.exception.ForbiddenException;

public class JsonUtil {

  private static ObjectMapper objectMapper = new ObjectMapper();

  public static String formatToString(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ForbiddenException("Error while serializing.", e);
    }
  }

  public static <T> T deserialize(String objectString, Class<T> className) {
    try {
      return objectMapper.readValue(objectString, className);
    } catch (IOException e) {
      throw new ForbiddenException("Error while parse json string.", e);
    }
  }
}
