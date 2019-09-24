package shamu.company.hashids;

import java.util.List;
import java.util.stream.Collectors;
import org.hashids.Hashids;

public class HashidsUtil {
  private static final String SALT_NAME = "shamu-hrs-salt";

  private static final Hashids hashids = new Hashids(SALT_NAME,22);


  public static String encode(final Object id) {
    if (id instanceof Long) {
      return hashids.encode((Long) id);
    }
    if (id instanceof List) {
      return encode((List<Long>) id);
    }
    return null;
  }

  public static String encode(final Long id) {
    return hashids.encode(id);
  }

  public static String encode(final List<Long> ids) {
    return ids.stream()
        .map(id -> hashids.encode(id))
        .collect(Collectors.toList()).toString();
  }

  public static Long decode(final String encodedId) {
    final long[] result = hashids.decode(encodedId);
    return result.length == 0 ? null : result[0];
  }
}
