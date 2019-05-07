package shamu.company.common.config;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.entity.ContentType;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import shamu.company.common.exception.AbstractException;
import shamu.company.common.exception.response.ErrorMessage;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DefaultExceptionFilter extends GenericFilterBean {

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    try {
      chain.doFilter(request, response);
    } catch (AbstractException abstractException) {
      response.setContentType(ContentType.APPLICATION_JSON.toString());
      response.setStatus(HttpStatus.BAD_REQUEST.value());

      ErrorMessage errorMessage = new ErrorMessage(abstractException.getType(),
          abstractException.getMessage());
      res.getWriter().write(JSON.toJSONString(errorMessage));
    }
  }
}
