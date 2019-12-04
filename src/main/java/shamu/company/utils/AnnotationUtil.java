package shamu.company.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public interface AnnotationUtil {

  static boolean fieldHasAnnotation(
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

  static <T extends Annotation> T getFieldAnnotation(
      final Object object,
      final String fieldName,
      final Class<T> annotationClazz) {
    Class c = object.getClass();
    while (c != Object.class) {
      try {
        final Field field = c.getDeclaredField(fieldName);
        return field.getDeclaredAnnotation(annotationClazz);
      } catch (final NoSuchFieldException e) {
        c = c.getSuperclass();
      }
    }
    return null;
  }

  static Object getFieldValue(
      final Object object,
      final String fieldName) {
    Class c = object.getClass();
    while (c != Object.class) {
      try {
        final Field field = c.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
      } catch (final NoSuchFieldException | IllegalAccessException e) {
        c = c.getSuperclass();
      }
    }
    return null;
  }
}
