package shamu.company.authorization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import shamu.company.authorization.Permission.Name;
import shamu.company.authorization.Type;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasAnyPermission {

  String targetId();

  Type targetType();

  Name[] permissions();
}
