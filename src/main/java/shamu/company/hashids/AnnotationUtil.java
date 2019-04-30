package shamu.company.hashids;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class AnnotationUtil {

  static boolean fieldHasAnnotation(Class clazz, String fieldName, Class annotationClazz) {
    Class c = clazz;
    while (c != Object.class) {
      try {
        Field field = c.getDeclaredField(fieldName);
        Annotation annotation = field.getAnnotation(annotationClazz);
        return annotation != null;
      } catch (NoSuchFieldException e) {
        c = c.getSuperclass();
      }
    }

    return false;
  }

  private AnnotationUtil() {

  }
}
