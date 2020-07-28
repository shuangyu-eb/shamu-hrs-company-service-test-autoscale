package shamu.company.account.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import shamu.company.utils.UuidUtil;

class AccountServiceTests {

  @Mock Auth0Helper auth0Helper;

  @Mock UserStatusService userStatusService;

  @Mock UserService userService;

  @InjectMocks AccountService accountService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class CreatePassword {

    private CreatePasswordDto createPasswordDto;

    private com.auth0.json.mgmt.users.User user;

    @BeforeEach
    void setUp() {
      createPasswordDto = new CreatePasswordDto();
      createPasswordDto.setEmailWork("example@indeed.com");
      final String password =
          RandomStringUtils.randomAlphabetic(4).toUpperCase()
              + RandomStringUtils.randomAlphabetic(4).toLowerCase()
              + RandomStringUtils.randomNumeric(4);
      final String resetPasswordToken = UUID.randomUUID().toString().replaceAll("-", "");
      createPasswordDto.setNewPassword(password);
      createPasswordDto.setResetPasswordToken(resetPasswordToken);
      user = new com.auth0.json.mgmt.users.User();
    }

    @Test
    void whenPasswordTokenNotMatch_thenShouldThrow() {
      final User targetUser = new User();
      targetUser.setResetPasswordToken(RandomStringUtils.randomAlphabetic(10));
      Mockito.when(userService.findByEmailWork(Mockito.anyString())).thenReturn(targetUser);
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> accountService.createPassword(createPasswordDto));
    }

    @Test
    void whenNoSuchUserInAuth0_thenShouldNotThrow() {
      final User targetUser = new User();
      targetUser.setResetPasswordToken(createPasswordDto.getResetPasswordToken());
      Mockito.when(userService.findByEmailWork(Mockito.anyString())).thenReturn(targetUser);
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any()))
          .thenReturn(new com.auth0.json.mgmt.users.User());
      Assertions.assertDoesNotThrow(() -> accountService.createPassword(createPasswordDto));
    }

    @Test
    void whenNoSuchUserInDatabase_thenShouldThrow() {
      final User targetUser = new User();
      targetUser.setResetPasswordToken(createPasswordDto.getResetPasswordToken());
      Mockito.when(userService.findByEmailWork(Mockito.anyString())).thenReturn(null);
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> accountService.createPassword(createPasswordDto));
    }

    @Test
    void whenResetTokenNotEqual_thenShouldThrow() {
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(user);
      final User targetUser = new User();
      targetUser.setResetPasswordToken(RandomStringUtils.randomAlphabetic(10));
      Mockito.when(userService.findByEmailWork(Mockito.anyString())).thenReturn(targetUser);
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> accountService.createPassword(createPasswordDto));
    }

    @Test
    void whenNoError_thenShouldSuccess() {
      final User targetUser = new User();
      targetUser.setResetPasswordToken(createPasswordDto.getResetPasswordToken());
      Mockito.when(userService.findByEmailWork(Mockito.anyString())).thenReturn(targetUser);
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(user);
      final UserStatus targetStatus = new UserStatus();
      targetStatus.setName(Status.ACTIVE.name());
      Mockito.when(userStatusService.findByName(Mockito.any())).thenReturn(targetStatus);

      Assertions.assertDoesNotThrow(() -> accountService.createPassword(createPasswordDto));
    }

    @Test
    void whenNoAuthUser_thenShouldThrow() {
      final User targetUser = new User();
      targetUser.setId(UuidUtil.getUuidString());
      targetUser.setResetPasswordToken(createPasswordDto.getResetPasswordToken());
      Mockito.when(userService.findByEmailWork(Mockito.anyString())).thenReturn(targetUser);
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(user);
      final UserStatus targetStatus = new UserStatus();
      targetStatus.setName(Status.ACTIVE.name());
      Mockito.when(userStatusService.findByName(Mockito.any())).thenReturn(targetStatus);
      Mockito.when(
              auth0Helper.getAuth0UserByIdWithByEmailFailover(
                  Mockito.anyString(), Mockito.anyString()))
          .thenThrow(new ResourceNotFoundException("", "", ""));

      Assertions.assertThrows(
          UserNotFoundByEmailException.class,
          () -> accountService.createPassword(createPasswordDto));
    }
  }

  @Nested
  class createPasswordAndInvitationTokenExist {

    String passwordToken, invitationToken;
    User currentUser;

    @BeforeEach
    void setUp() {
      currentUser = new User();
      passwordToken = "a";
      invitationToken = "b";
      currentUser.setInvitationEmailToken(passwordToken);
      currentUser.setResetPasswordToken(invitationToken);
      currentUser.setUserStatus(new UserStatus("ACTIVE"));
      currentUser.setInvitedAt(Timestamp.from(Instant.now()));
    }

    @Test
    void whenUserIsNull_thenShouldThrow() {
      Mockito.when(
              userService.findByInvitationEmailTokenAndResetPasswordToken(
                  passwordToken, invitationToken))
          .thenReturn(null);

      Assertions.assertThrows(
          UserNotFoundByInvitationTokenException.class,
          () ->
              accountService.createPasswordAndInvitationTokenExist(passwordToken, invitationToken));
    }

    @Test
    void whenUserExistInvitedExpired_thenShouldThrow() {
      currentUser.setInvitedAt(Timestamp.valueOf(LocalDateTime.now().minus(100, ChronoUnit.HOURS)));
      Mockito.when(
              userService.findByInvitationEmailTokenAndResetPasswordToken(
                  Mockito.any(), Mockito.any()))
          .thenReturn(currentUser);
      Assertions.assertThrows(
          EmailExpiredException.class,
          () ->
              accountService.createPasswordAndInvitationTokenExist(passwordToken, invitationToken));
    }

    @Test
    void whenUserExistInvitedNotExpired_thenShouldReturnTrue() {
      currentUser.setInvitedAt(Timestamp.valueOf(LocalDateTime.now().minus(1, ChronoUnit.HOURS)));
      Mockito.when(
              userService.findByInvitationEmailTokenAndResetPasswordToken(
                  Mockito.any(), Mockito.any()))
          .thenReturn(currentUser);
      Assertions.assertTrue(
          accountService.createPasswordAndInvitationTokenExist(passwordToken, invitationToken));
    }
  }
}
