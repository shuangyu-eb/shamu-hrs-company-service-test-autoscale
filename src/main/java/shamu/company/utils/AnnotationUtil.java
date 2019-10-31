package shamu.company.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AnnotationUtil {

  private AnnotationUtil() {

  }

  public static boolean fieldHasAnnotation(
      final Class clazz,
      final String fieldName,
      final Class annotationClazz) {
    Class c = clazz;
    while (c != Object.class) {
      try {
        final Field field = c.getDeclaredField(fieldName);
        final Annotation annotation = field.getAnnotation(annotationClazz);
        return annotation != null;
      } catch (final NoSuchFieldException e) {
        c = c.getSuperclass();
      }
    }

    return false;
  }
}
