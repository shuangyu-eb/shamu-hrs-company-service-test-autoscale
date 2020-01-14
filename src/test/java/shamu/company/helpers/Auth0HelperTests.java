package shamu.company.helpers;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.UsersEntity;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
import com.auth0.json.mgmt.users.UsersPage;
import com.auth0.net.Request;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.NonUniqueAuth0ResourceException;
import shamu.company.helpers.auth0.Auth0Config;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.helpers.auth0.Auth0Manager;
import shamu.company.user.entity.User.Role;

class Auth0HelperTests {

  private final Auth0Config auth0Config = new Auth0Config();
  private Auth0Helper auth0Helper;
  @Mock private Auth0Manager auth0Manager;
  @Mock private ManagementAPI managementAPI;
  @Mock private UsersEntity usersEntity;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);

    Mockito.when(auth0Manager.getManagementApi()).thenReturn(managementAPI);
    Mockito.when(managementAPI.users()).thenReturn(usersEntity);

    auth0Helper = new Auth0Helper(auth0Manager, auth0Config);
  }

  @Test
  void testUpdatePassword() {

    final Request mockedRequest = Mockito.mock(Request.class);
    Mockito.when(usersEntity.update(Mockito.any(), Mockito.any(User.class)))
        .thenReturn(mockedRequest);
    Assertions.assertDoesNotThrow(
        () -> auth0Helper.updatePassword(new User(), RandomStringUtils.randomAlphabetic(10)));
  }

  @Test
  void testUpdateVerified() {

    final Request mockedRequest = Mockito.mock(Request.class);
    Mockito.when(usersEntity.update(Mockito.any(), Mockito.any(User.class)))
        .thenReturn(mockedRequest);
    Assertions.assertDoesNotThrow(() -> auth0Helper.updateVerified(new User(), true));
  }

  @Test
  void testGetUserId() {
    final User user = new User();

    final Map<String, Object> appMetadata = new HashMap<>();
    final String userId = RandomStringUtils.randomAlphabetic(10);
    appMetadata.put("id", userId);
    user.setAppMetadata(appMetadata);
    final String resultUserId = auth0Helper.getUserId(user);
    Assertions.assertEquals(userId, resultUserId);
  }

  @Test
  void testAddUser() throws Auth0Exception {
    final Request mockedRequest = Mockito.mock(Request.class);
    Mockito.when(usersEntity.create(Mockito.any())).thenReturn(mockedRequest);
    auth0Helper.addUser("example@indeed.com", null, Role.EMPLOYEE.getValue());
    Mockito.verify(mockedRequest, Mockito.times(1)).execute();
  }

  @Nested
  class GetUserByUserIdFromAuth0 {

    Request mockedUserRequest;

    @BeforeEach
    void setUp() {
      mockedUserRequest = Mockito.mock(Request.class);
      Mockito.when(usersEntity.list(Mockito.any())).thenReturn(mockedUserRequest);
    }

    @Test
    void whenUserMoreThanOne_thenShouldThrow() throws Auth0Exception {
      final UsersPage mockUsersPage = Mockito.mock(UsersPage.class);
      final List<User> fakeUsersResult = new ArrayList<>(2);
      fakeUsersResult.add(new User());
      fakeUsersResult.add(new User());

      Mockito.when(mockUsersPage.getItems()).thenReturn(fakeUsersResult);

      Mockito.when(mockedUserRequest.execute()).thenReturn(mockUsersPage);
      Assertions.assertThrows(
          NonUniqueAuth0ResourceException.class,
          () -> {
            auth0Helper.getUserByUserIdFromAuth0(RandomStringUtils.randomAlphabetic(10));
          });
    }

    @Test
    void whenHasOneUser_thenShouldSuccess() throws Auth0Exception {
      final UsersPage mockUsersPage = Mockito.mock(UsersPage.class);
      final List<User> fakeUsersResult = new ArrayList<>(1);
      fakeUsersResult.add(new User());

      Mockito.when(mockUsersPage.getItems()).thenReturn(fakeUsersResult);
      Mockito.when(mockedUserRequest.execute()).thenReturn(mockUsersPage);
      Assertions.assertDoesNotThrow(
          () -> {
            auth0Helper.getUserByUserIdFromAuth0(RandomStringUtils.randomAlphabetic(10));
          });
    }
  }
}
