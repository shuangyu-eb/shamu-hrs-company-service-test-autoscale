package shamu.company.hashids;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.LongCodec;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.amazonaws.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import org.springframework.http.HttpInputMessage;
import shamu.company.common.exception.GeneralException;

public class Converter extends FastJsonHttpMessageConverter {

  @Override
  public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) {
    try {
      InputStream in = inputMessage.getBody();
      byte[] bytes = IOUtils.toByteArray(in);
      Charset charset = getFastJsonConfig().getCharset();
      if (charset == null) {
        charset = com.alibaba.fastjson.util.IOUtils.UTF8;
      }
      String json = new String(bytes, charset);

      if (type == String.class) {
        return json;
      } else if (type == Long.class) {
        return Long.parseLong(json);
      }

      return JSON.parseObject(json, type, getFastJsonConfig().getParserConfig(),
          JSON.DEFAULT_PARSER_FEATURE, getFastJsonConfig().getFeatures());
    } catch (JSONException ex) {
      throw new GeneralException("JSON parse error: " + ex.getMessage(), ex);
    } catch (IOException ex) {
      throw new GeneralException("I/O error while reading input message", ex);
    }
  }

  private class LongDecode extends LongCodec {

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
      String field;
      if (fieldName instanceof String) {
        field = fieldName.toString();
      } else {
        field = (String) parser.getContext().fieldName;
      }

      Object contentObject = parser.getContext().object;
      if (contentObject instanceof List) {
        contentObject = parser.getContext().parent.object;
      }

      if (contentObject != null && AnnotationUtil
          .fieldHasAnnotation(contentObject.getClass(), field,
              HashidsFormat.class)) {
        JSONLexer lexer = parser.lexer;
        int token = lexer.token();
        if (token == JSONToken.NULL) {
          lexer.nextToken(JSONToken.COMMA);
          return null;
        }
        // String => Long
        if (token == JSONToken.LITERAL_STRING) {
          String value = (String) parser.parse();
          if (!value.matches("\\d+")) {
            Long id = HashidsUtil.decode(value);
            return (T) id;
          }
        }
      }
      return super.deserialze(parser, clazz, fieldName);
    }
  }

  private ValueFilter getSerializeFilter() {
    return (object, name, value) -> {
      if (object != null && value != null && AnnotationUtil
          .fieldHasAnnotation(object.getClass(), name, HashidsFormat.class)) {
        return HashidsUtil.encode(value);
      }
      return value;
    };
  }

  Converter() {
    super();
    FastJsonConfig fastJsonConfig = new FastJsonConfig();
    LongDecode longDecode = new LongDecode();
    // id => hash id
    fastJsonConfig.setSerializeFilters(getSerializeFilter());
    // not filter the field when the value is null
    fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteMapNullValue);

    //hash id => id
    ParserConfig parserConfig = new ParserConfig();
    parserConfig.putDeserializer(Long.class, longDecode);
    parserConfig.putDeserializer(long.class, longDecode);
    fastJsonConfig.setParserConfig(parserConfig);
    this.setFastJsonConfig(fastJsonConfig);
  }
}
