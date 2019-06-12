package shamu.company.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.util.stream.Stream;
import shamu.company.common.exception.GeneralException;

public class BeanUtils {

  //merge two bean by discovering differences
  public static <T> void merge(T source, T target) {
    Class clazz = target.getClass();
    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
      // Iterate over all the attributes
      Stream.of(beanInfo.getPropertyDescriptors()).forEach(descriptor -> {

        // Only copy writable attributes
        if (descriptor.getWriteMethod() != null) {
          try {
            Object originalValue = descriptor.getReadMethod().invoke(target);
            // Only copy values values where the target values is null
            if (originalValue == null) {
              Object defaultValue = descriptor.getReadMethod().invoke(source);
              descriptor.getWriteMethod().invoke(target, defaultValue);
            }
          } catch (Exception e) {
            e.printStackTrace();
            throw new GeneralException("merge class: " + clazz + " failed!");
          }
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      throw new GeneralException("merge class: " + clazz + " failed!");
    }
  }
}
