package shamu.company.common;

import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import shamu.company.utils.ValidationUtil;

@Component
@Aspect
public class RepositoryAdvice {

  @Before("execution(* org.springframework.data.jpa.repository.JpaRepository+.save(Object))")
  public void beforeSave(final JoinPoint point) {
    final Object[] args = point.getArgs();
    if (args.length == 1) {
      ValidationUtil.validate(args[0]);
    }
  }

  @Before("execution(* org.springframework.data.jpa.repository.JpaRepository+.saveAll(Iterable))")
  public void beforeSaveAll(final JoinPoint point) {
    final Object[] args = point.getArgs();
    if (args.length == 1) {
      ValidationUtil.validate((List<Object>) args[0]);
    }
  }
}
