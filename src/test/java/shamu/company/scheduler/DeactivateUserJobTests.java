package shamu.company.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.scheduler.job.DeactivateUserJob;
import shamu.company.user.dto.UserStatusUpdateDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.service.UserService;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

public class DeactivateUserJobTests {
  private static DeactivateUserJob deactivateUserJob;

  @Mock private UserService userService;

  @Mock private JobExecutionContext jobExecutionContext;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    deactivateUserJob = new DeactivateUserJob(userService);
  }

  @Nested
  class executeJob {
    UserStatusUpdateDto userStatusUpdateDto;
    User user;
    String userId;

    @BeforeEach
    void setUp() {
      userStatusUpdateDto = new UserStatusUpdateDto();
      userStatusUpdateDto.setUserStatus(UserStatus.Status.ACTIVE);
      userStatusUpdateDto.setDeactivationReason(new SelectFieldInformationDto("id", "name"));
      user = new User();
      final UserRole userRole = new UserRole();
      userId = UUID.randomUUID().toString().replaceAll("-", "");
      userRole.setName(User.Role.ADMIN.name());
      user.setUserRole(userRole);
      user.setId(userId);
    }

    @Test
    void whenJobExecutionContextIsValid_thenShouldSuccess() {
      final Map<String, Object> jobParameter = new HashMap<>();
      jobParameter.put("UserStatusUpdateDto", JsonUtil.formatToString(userStatusUpdateDto));
      jobParameter.put("User", JsonUtil.formatToString(user));
      jobParameter.put("companyId", JsonUtil.formatToString(UuidUtil.getUuidString()));
      Mockito.when(jobExecutionContext.getMergedJobDataMap())
          .thenReturn(new JobDataMap(jobParameter));

      Assertions.assertDoesNotThrow(() -> deactivateUserJob.executeInternal(jobExecutionContext));
    }
  }
}
