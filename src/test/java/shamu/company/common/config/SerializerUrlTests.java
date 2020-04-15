package shamu.company.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.helpers.s3.AwsHelper;

import java.io.IOException;

public class SerializerUrlTests {

  @Mock private AwsHelper awsHelper;
  private JsonGenerator jsonGenerator;
  private SerializerProvider serializerProvider;
  private SerializerUrl serializerUrl;


  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    serializerUrl = new SerializerUrl(awsHelper);
    jsonGenerator = Mockito.mock(JsonGenerator.class);
    serializerProvider = Mockito.mock(SerializerProvider.class);
  }

  @Test
  void whenValueNotBlankAndNotStartWithHttp_thenShouldWriteString() throws IOException {
    Mockito.when(awsHelper.findPreSignedUrl(Mockito.anyString())).thenReturn("1");
    serializerUrl.serialize("1",jsonGenerator,serializerProvider);
    Mockito.verify(jsonGenerator,Mockito.times(1)).writeString("1");
  }

  @Test
  void whenValueBlank_thenShouldWriteNull() throws IOException {
    serializerUrl.serialize("http://",jsonGenerator,serializerProvider);
    Mockito.verify(jsonGenerator,Mockito.times(1)).writeNull();
  }
}
