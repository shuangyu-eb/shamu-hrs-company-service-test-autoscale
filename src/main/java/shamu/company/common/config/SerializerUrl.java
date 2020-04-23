package shamu.company.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import shamu.company.helpers.s3.AwsHelper;

public class SerializerUrl extends JsonSerializer<String> {

  private final AwsHelper awsHelper;

  @Autowired
  public SerializerUrl(final AwsHelper awsHelper) {
    this.awsHelper = awsHelper;
  }

  @Override
  public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializers)
      throws IOException {
    if (Strings.isNotBlank(value) && !value.startsWith("http")) {
      jsonGenerator.writeString(awsHelper.findPreSignedUrl(value));
    } else {
      jsonGenerator.writeNull();
    }
  }
}
