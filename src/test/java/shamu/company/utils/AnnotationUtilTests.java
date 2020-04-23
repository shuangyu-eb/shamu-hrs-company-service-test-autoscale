package shamu.company.utils;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AnnotationUtilTests {

  @Target({FIELD})
  @Retention(RUNTIME)
  @interface TestInterface {}

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class SuperObject {

    @TestInterface String superHasAnnotationField;

    String superHasNoAnnotationField;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  class TestObject extends SuperObject {

    @TestInterface String hasAnnotationField;

    String hasNoAnnotationField;
  }

  @Nested
  class TestFieldHasAnnotation {

    @Test
    void whenFieldHasAnnotation_thenReturnTrue() {
      final boolean result =
          AnnotationUtil.fieldHasAnnotation(
              TestObject.class, "hasAnnotationField", TestInterface.class);
      Assertions.assertTrue(result);
    }

    @Test
    void whenFieldHasNoAnnotation_thenReturnFalse() {
      final boolean result =
          AnnotationUtil.fieldHasAnnotation(
              TestObject.class, "hasNoAnnotationField", TestInterface.class);
      Assertions.assertFalse(result);
    }

    @Test
    void whenFieldBelongToSuper_fieldHasAnnotation_thenReturnTrue() {
      final boolean result =
          AnnotationUtil.fieldHasAnnotation(
              TestObject.class, "superHasAnnotationField", TestInterface.class);
      Assertions.assertTrue(result);
    }

    @Test
    void whenFieldNotExists_thenReturnFalse() {
      final boolean result =
          AnnotationUtil.fieldHasAnnotation(TestObject.class, "anything", TestInterface.class);
      Assertions.assertFalse(result);
    }
  }

  @Nested
  class TestGetFieldAnnotation {

    TestObject testObject;

    @BeforeEach
    void init() {
      testObject = new TestObject();
    }

    @Test
    void whenFieldHasAnnotation_thenReturnTrue() {
      final Optional result =
          AnnotationUtil.getFieldAnnotation(testObject, "hasAnnotationField", TestInterface.class);
      final String annotationPath = TestInterface.class.toString().replace("interface ", "");

      Assertions.assertTrue(result.isPresent());
      Assertions.assertTrue(result.get().toString().contains(annotationPath));
    }

    @Test
    void whenFieldBelongToSuper_fieldHasAnnotation_thenReturnTrue() {
      final Optional result =
          AnnotationUtil.getFieldAnnotation(
              testObject, "superHasAnnotationField", TestInterface.class);
      final String annotationPath = TestInterface.class.toString().replace("interface ", "");

      Assertions.assertTrue(result.isPresent());
      Assertions.assertTrue(result.get().toString().contains(annotationPath));
    }

    @Test
    void whenFieldNotExists_thenReturnFalse() {
      final Optional result =
          AnnotationUtil.getFieldAnnotation(testObject, "any", TestInterface.class);

      Assertions.assertFalse(result.isPresent());
    }
  }

  @Nested
  class testGetFieldValue {

    TestObject testObject;

    @BeforeEach
    void init() {
      testObject = new TestObject();
    }

    @Test
    void whenFieldHasAnnotation_thenReturnResult() {
      final String value = "value";
      testObject.setHasNoAnnotationField(value);
      final Optional result = AnnotationUtil.getFieldValue(testObject, "hasNoAnnotationField");

      Assertions.assertTrue(result.isPresent());
      Assertions.assertEquals(value, result.get());
    }

    @Test
    void whenFieldBelongToSuper_fieldHasAnnotation_thenReturnResult() {
      final String value = "value";
      testObject.setSuperHasNoAnnotationField(value);
      final Optional result = AnnotationUtil.getFieldValue(testObject, "superHasNoAnnotationField");

      Assertions.assertTrue(result.isPresent());
      Assertions.assertEquals(value, result.get());
    }

    @Test
    void whenFieldNotExists_thenReturnEmpty() {
      final Optional result = AnnotationUtil.getFieldValue(testObject, "any");

      Assertions.assertFalse(result.isPresent());
    }
  }
}
