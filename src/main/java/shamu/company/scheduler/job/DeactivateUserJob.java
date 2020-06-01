package shamu.company.scheduler.job;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import shamu.company.user.dto.UserStatusUpdateDto;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;
import shamu.company.utils.JsonUtil;

@Component
public class DeactivateUserJob extends QuartzJobBean {
  private final UserService userService;

  @Autowired
  public DeactivateUserJob(final UserService userService) {
    this.userService = userService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {
    final String userStatusUpdateJson =
        String.valueOf(jobExecutionContext.getMergedJobDataMap().get("UserStatusUpdateDto"));
    final String userJson = String.valueOf(jobExecutionContext.getMergedJobDataMap().get("User"));

    userService.deactivateUser(
        JsonUtil.deserialize(userStatusUpdateJson, UserStatusUpdateDto.class),
        JsonUtil.deserialize(userJson, User.class));
  }
}
