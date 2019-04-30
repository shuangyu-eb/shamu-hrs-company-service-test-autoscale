package shamu.company.hashids;

import java.util.List;
import java.util.stream.Collectors;
import org.hashids.Hashids;

public class HashidsUtil {

  private static Hashids hashids = new Hashids();


  public static String encode(Object id) {
    if (id instanceof Long) {
      return hashids.encode((Long) id);
    }
    if (id instanceof List) {
      return encode((List<Long>) id);
    }
    return null;
  }

  public static String encode(Long id) {
    return hashids.encode(id);
  }

  public static String encode(List<Long> ids) {
    return ids.stream()
        .map(id -> hashids.encode(id))
        .collect(Collectors.toList()).toString();
  }

  public static Long decode(String encodedId) {
    try {
      return hashids.decode(encodedId)[0];
    } catch (Exception e) {
      return null;
    }
  }
}
