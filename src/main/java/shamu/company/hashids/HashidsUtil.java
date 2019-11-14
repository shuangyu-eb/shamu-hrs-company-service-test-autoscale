//package shamu.company.hashids;
//
//import java.util.List;
//import java.util.stream.Collectors;
//import org.hashids.Hashids;
//
//public class HashidsUtil {
//  private static final String SALT_NAME = "shamu-hrs-salt";
//
//  private static final Hashids hashids = new Hashids(SALT_NAME,22);
//
//
//  public static String encode(final Object id) {
//    if (id instanceof String) {
//      return hashids.encode((String) id);
//    }
//    if (id instanceof List) {
//      return encode((List<String>) id);
//    }
//    return null;
//  }
//
//  public static String encode(final String id) {
//    return hashids.encode(id);
//  }
//
//  public static String encode(final List<String> ids) {
//    return ids.stream()
//        .map(id -> hashids.encode(id))
//        .collect(Collectors.toList()).toString();
//  }
//
//  public static String decode(final String encodedId) {
//    final String[] result = hashids.decode(encodedId);
//    return result.length == 0 ? null : result[0];
//  }
//}
