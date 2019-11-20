package shamu.company.common.config;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.serializer.StringCodec;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import org.springframework.util.ReflectionUtils;
import shamu.company.common.config.annotations.CanEmpty;

public class StringFieldJsonDecoder extends StringCodec {

  @Override
  public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
    // String in an array
    if (!fieldName.getClass().isAssignableFrom(String.class)) {
      return super.deserialze(parser, clazz, fieldName);
    }

    Class targetClass = parser.getContext().object.getClass();
    Field field = ReflectionUtils.findField(targetClass, (String) fieldName);

    if (field == null) {
      return super.deserialze(parser, clazz, fieldName);
    }

    CanEmpty canEmptyAnnotation = field.getAnnotation(CanEmpty.class);
    String result = (String)parser.parse();
    if (result == null
        || (result.isEmpty() && canEmptyAnnotation == null)) {
      return null;
    }

    return (T) result;
  }
}
