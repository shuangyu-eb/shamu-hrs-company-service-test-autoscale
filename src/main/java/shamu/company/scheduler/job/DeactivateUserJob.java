package shamu.company.scheduler.job;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import shamu.company.scheduler.QuartzUtil;
import shamu.company.user.dto.UserStatusUpdateDto;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@Component
public class DeactivateUserJob extends QuartzJobBean {
  private final UserService userService;

  @Autowired
  public DeactivateUserJob(final UserService userService) {
    this.userService = userService;
  }

  @Override
  public void executeInternal(final JobExecutionContext jobExecutionContext) {
    final UserStatusUpdateDto userStatusUpdateDto =
        QuartzUtil.getParameter(
            jobExecutionContext, "UserStatusUpdateDto", UserStatusUpdateDto.class);
    final User user = QuartzUtil.getParameter(jobExecutionContext, "User", User.class);

    userService.deactivateUser(userStatusUpdateDto, user);
  }
}
