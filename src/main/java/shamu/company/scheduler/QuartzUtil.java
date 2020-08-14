package shamu.company.scheduler;

import org.quartz.JobExecutionContext;
import shamu.company.utils.JsonUtil;

public class QuartzUtil {

  private QuartzUtil() {}

  public static <T> T getParameter(
      final JobExecutionContext jobExecutionContext,
      final String parameterName,
      final Class<T> className) {
    final String jsonStr =
        String.valueOf(jobExecutionContext.getMergedJobDataMap().get(parameterName));
    return JsonUtil.deserialize(jsonStr, className);
  }
}
