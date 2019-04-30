package shamu.company.hashids;

import java.text.ParseException;
import java.util.Locale;
import org.springframework.format.Formatter;

public class HashidsFormatter implements Formatter<Long> {

  @Override
  public Long parse(String s, Locale locale) throws ParseException {
    return HashidsUtil.decode(s);
  }

  @Override
  public String print(Long id, Locale locale) {
    return HashidsUtil.encode(id);
  }
}
