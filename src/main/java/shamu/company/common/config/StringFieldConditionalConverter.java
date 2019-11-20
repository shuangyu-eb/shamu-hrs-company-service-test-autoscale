package shamu.company.common.config;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import shamu.company.common.config.annotations.CanEmpty;

public class StringFieldConditionalConverter implements GenericConverter {

  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    ConvertiblePair convertiblePair = new ConvertiblePair(String.class, String.class);
    Set<ConvertiblePair> convertiblePairs = new HashSet<>();
    convertiblePairs.add(convertiblePair);
    return convertiblePairs;
  }

  @Override
  public Object convert(Object o, TypeDescriptor sourceTypeDescriptor,
      TypeDescriptor targetTypeDescriptor) {
    CanEmpty annotation = targetTypeDescriptor.getAnnotation(CanEmpty.class);
    if (!StringUtils.isEmpty((String) o)
        || annotation != null) {
      return o;
    }

    return null;
  }
}
