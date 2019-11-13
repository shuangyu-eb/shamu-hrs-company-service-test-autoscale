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
import shamu.company.utils.AnnotationUtil;

public class Converter extends FastJsonHttpMessageConverter {

  public Converter(final ValueFilter valueFilter, final ValueFilter valueFilter2) {
    super();
    final FastJsonConfig fastJsonConfig = new FastJsonConfig();
    final LongDecode longDecode = new LongDecode();
    // id => hash id
    fastJsonConfig.setSerializeFilters(getSerializeFilter(), valueFilter, valueFilter2);
    // not filter the field when the value is null
    fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteMapNullValue);

    //hash id => id
    final ParserConfig parserConfig = new ParserConfig();
    parserConfig.putDeserializer(Long.class, longDecode);
    parserConfig.putDeserializer(long.class, longDecode);
    fastJsonConfig.setParserConfig(parserConfig);
    this.setFastJsonConfig(fastJsonConfig);
  }

  @Override
  public Object read(final Type type, final Class<?> contextClass,
      final HttpInputMessage inputMessage) {
    try {
      final InputStream in = inputMessage.getBody();
      final byte[] bytes = IOUtils.toByteArray(in);
      Charset charset = getFastJsonConfig().getCharset();
      if (charset == null) {
        charset = com.alibaba.fastjson.util.IOUtils.UTF8;
      }
      final String json = new String(bytes, charset);

      if (type == String.class) {
        return json;
      } else if (type == Long.class) {
        return Long.parseLong(json);
      }

      return JSON.parseObject(json, type, getFastJsonConfig().getParserConfig(),
          JSON.DEFAULT_PARSER_FEATURE, getFastJsonConfig().getFeatures());
    } catch (final JSONException ex) {
      throw new GeneralException("JSON parse error: " + ex.getMessage(), ex);
    } catch (final IOException ex) {
      throw new GeneralException("I/O error while reading input message", ex);
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

  private class LongDecode extends LongCodec {

    @Override
    public <T> T deserialze(final DefaultJSONParser parser,
        final Type clazz, final Object fieldName) {
      final String field;
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
        final JSONLexer lexer = parser.lexer;
        final int token = lexer.token();
        if (token == JSONToken.NULL) {
          lexer.nextToken(JSONToken.COMMA);
          return null;
        }
        // String => Long
        if (token == JSONToken.LITERAL_STRING) {
          final String value = (String) parser.parse();
          final Long id = HashidsUtil.decode(value);
          return (T) id;
        }
      }
      return super.deserialze(parser, clazz, fieldName);
    }
  }
}
