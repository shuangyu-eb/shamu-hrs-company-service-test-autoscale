package shamu.company.helpers.s3;

import com.alibaba.fastjson.serializer.ValueFilter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shamu.company.utils.AnnotationUtil;

@Component
public class PreSignedValueFilter implements ValueFilter {

  private final AwsHelper awsHelper;

  @Autowired
  public PreSignedValueFilter(final AwsHelper awsHelper) {
    this.awsHelper = awsHelper;
  }


  @Override
  public Object process(final Object object, final String name, final Object value) {
    if (object != null && value instanceof String
        && AnnotationUtil.fieldHasAnnotation(object.getClass(), name, PreSinged.class)
        && !Strings.isBlank((String) value)
        && !((String) value).startsWith("http")) {
      return awsHelper.findPreSignedUrl((String) value);
    }
    return value;
  }
}
