package shamu.company.authorization;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import shamu.company.authorization.annotation.HasAnyPermission;
import shamu.company.authorization.annotation.HasAuthority;
import shamu.company.authorization.annotation.HasPermission;
import shamu.company.common.exception.ForbiddenException;

@Aspect
@Component
public class Authority {

  private ExpressionParser parser = new SpelExpressionParser();
  
  private LocalVariableTableParameterNameDiscoverer discoverer =
      new LocalVariableTableParameterNameDiscoverer();

  private static final String errorMessage = "You don't have the permission: ";

  @Autowired
  PermissionUtils permissionUtils;

  @Around("@annotation(hasAnyPermission)")
  public Object invoked(ProceedingJoinPoint pjp, HasAnyPermission hasAnyPermission)
      throws Throwable {
    Long id = this.getId(pjp, hasAnyPermission.targetId());
    Permission.Name[] permissions = hasAnyPermission.permissions();
    Type type = hasAnyPermission.targetType();

    boolean result = permissionUtils.hasAnyPermission(id, type, permissions);
    if (!result) {
      throw new ForbiddenException(errorMessage + Arrays.asList(permissions));
    }

    return pjp.proceed();
  }

  @Around("@annotation(hasPermission)")
  public Object invoked(ProceedingJoinPoint pjp, HasPermission hasPermission)
      throws Throwable {
    Long id = this.getId(pjp, hasPermission.targetId());
    Permission.Name permissions = hasPermission.permission();
    Type type = hasPermission.targetType();

    boolean result = permissionUtils.hasPermission(id, type, permissions);
    if (!result) {
      throw new ForbiddenException(errorMessage + Arrays.asList(permissions));
    }

    return pjp.proceed();
  }

  @Around("@annotation(hasAuthority)")
  public Object invoked(ProceedingJoinPoint pjp, HasAuthority hasAuthority)
      throws Throwable {
    Permission.Name permission = hasAuthority.permission();

    boolean result = permissionUtils.hasAuthority(permission);
    if (!result) {
      throw new ForbiddenException(errorMessage + permission);
    }

    return pjp.proceed();
  }

  private Long getId(ProceedingJoinPoint pjp, String spel) {
    Object[] args = pjp.getArgs();
    Method method = ((MethodSignature) pjp.getSignature()).getMethod();
    String[] params = discoverer.getParameterNames(method);

    EvaluationContext context = new StandardEvaluationContext();
    for (int len = 0; len < params.length; len++) {
      context.setVariable(params[len], args[len]);
    }

    Expression expression = parser.parseExpression(spel);
    return expression.getValue(context, Long.class);
  }
}
