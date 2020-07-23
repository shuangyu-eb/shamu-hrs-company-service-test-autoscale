package shamu.company.account.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.user.dto.CreatePasswordDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.exception.errormapping.EmailExpiredException;
import shamu.company.user.exception.errormapping.UserNotFoundByEmailException;
import shamu.company.user.exception.errormapping.UserNotFoundByInvitationTokenException;
import shamu.company.user.service.UserService;
import shamu.company.user.service.UserStatusService;

@Service
public class AccountService {

  private final Auth0Helper auth0Helper;

  private final UserStatusService userStatusService;

  private final UserService userService;

  public AccountService(
      final Auth0Helper auth0Helper,
      final UserStatusService userStatusService,
      @Lazy final UserService userService) {
    this.auth0Helper = auth0Helper;
    this.userStatusService = userStatusService;
    this.userService = userService;
  }

  public boolean createPasswordAndInvitationTokenExist(
      final String passwordToken, final String invitationToken) {
    final User user =
        userService.findByInvitationEmailTokenAndResetPasswordToken(invitationToken, passwordToken);
    if (user == null) {
      throw new UserNotFoundByInvitationTokenException(
          String.format("User with invitationToken %s not found!", invitationToken),
          invitationToken);
    }
    if (Timestamp.from(Instant.now())
        .after(
            Timestamp.valueOf(user.getInvitedAt().toLocalDateTime().plus(72, ChronoUnit.HOURS)))) {
      throw new EmailExpiredException("Email is expired");
    }
    return true;
  }

  public void createPassword(final CreatePasswordDto createPasswordDto) {
    final String userWorkEmail = createPasswordDto.getEmailWork();
    final User user = userService.findByEmailWork(userWorkEmail);

    if (user == null
        || !createPasswordDto.getResetPasswordToken().equals(user.getResetPasswordToken())) {
      throw new ResourceNotFoundException(
          String.format("User with email %s not found!", userWorkEmail), userWorkEmail, "user");
    }

    final com.auth0.json.mgmt.users.User authUser;

    try {
      authUser = auth0Helper.getAuth0UserByIdWithByEmailFailover(user.getId(), userWorkEmail);
    } catch (final ResourceNotFoundException e) {
      throw new UserNotFoundByEmailException(
          String.format("User with email %s not found.", createPasswordDto.getEmailWork()),
          createPasswordDto.getEmailWork());
    }

    auth0Helper.updatePassword(authUser, createPasswordDto.getNewPassword());
    auth0Helper.updateVerified(authUser, true);

    final UserStatus userStatus = userStatusService.findByName(Status.ACTIVE.name());
    user.setUserStatus(userStatus);
    user.setResetPasswordToken(null);
    userService.save(user);
  }
}
