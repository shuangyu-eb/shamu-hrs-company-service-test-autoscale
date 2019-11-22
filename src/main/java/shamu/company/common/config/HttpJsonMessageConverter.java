package shamu.company.common.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ParseProcess;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.amazonaws.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import org.springframework.http.HttpInputMessage;
import shamu.company.common.exception.GeneralException;

public class HttpJsonMessageConverter extends FastJsonHttpMessageConverter {

  public HttpJsonMessageConverter(final ValueFilter... valueFilter) {
    super();
    final FastJsonConfig fastJsonConfig = new FastJsonConfig();
    fastJsonConfig.setSerializeFilters(valueFilter);

    fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteMapNullValue);
    ParserConfig parserConfig = new ParserConfig();
    parserConfig.putDeserializer(String.class, new StringFieldJsonDecoder());
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

      ParserConfig parseConfig = getFastJsonConfig().getParserConfig();
      ParseProcess parseProcess = getFastJsonConfig().getParseProcess();
      return JSON.parseObject(json, type, parseConfig,
          parseProcess, JSON.DEFAULT_PARSER_FEATURE,
          getFastJsonConfig().getFeatures());
    } catch (final JSONException ex) {
      throw new GeneralException("JSON parse error: " + ex.getMessage(), ex);
    } catch (final IOException ex) {
      throw new GeneralException("I/O error while reading input message", ex);
    }
  }
}
