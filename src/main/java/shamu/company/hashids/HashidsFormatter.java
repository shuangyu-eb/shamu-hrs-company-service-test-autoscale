//package shamu.company.hashids;
//
//import java.text.ParseException;
//import java.util.Locale;
//import org.springframework.format.Formatter;
//
//public class HashidsFormatter implements Formatter<String> {
//
//  @Override
//  public String parse(String s, Locale locale) throws ParseException {
//    return HashidsUtil.decode(s);
//  }
//
//  @Override
//  public String print(String id, Locale locale) {
//    return HashidsUtil.encode(id);
//  }
//}
