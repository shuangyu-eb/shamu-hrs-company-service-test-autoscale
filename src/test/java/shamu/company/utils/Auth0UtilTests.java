package shamu.company.utils;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.UsersEntity;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;
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
import shamu.company.common.exception.GeneralAuth0Exception;
import shamu.company.common.exception.GeneralException;
import shamu.company.user.entity.User.Role;

class Auth0UtilTests {

  private final Auth0Config auth0Config = new Auth0Config();
  private Auth0Util auth0Util;
  @Mock
  private Auth0Manager auth0Manager;
  @Mock
  private ManagementAPI managementAPI;
  @Mock
  private UsersEntity usersEntity;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    auth0Util = new Auth0Util(auth0Manager, auth0Config);

    Mockito.when(auth0Manager.getManagementApi()).thenReturn(managementAPI);
    Mockito.when(managementAPI.users()).thenReturn(usersEntity);

    auth0Config.setClientId("clientId");
    auth0Config.setClientSecret("clientSecret");
    auth0Config.setDomain("test.auth0.com");
    auth0Util = new Auth0Util(auth0Manager, auth0Config);
  }

  @Test
  void testUpdatePassword() {

    final Request mockedRequest = Mockito.mock(Request.class);
    Mockito.when(usersEntity.update(Mockito.any(), Mockito.any(User.class)))
        .thenReturn(mockedRequest);
    Assertions.assertDoesNotThrow(() -> auth0Util.updatePassword(new User(),
        RandomStringUtils.randomAlphabetic(10)));
  }

  @Test
  void testUpdateVerified() {

    final Request mockedRequest = Mockito.mock(Request.class);
    Mockito.when(usersEntity.update(Mockito.any(), Mockito.any(User.class)))
        .thenReturn(mockedRequest);
    Assertions.assertDoesNotThrow(() -> auth0Util.updateVerified(new User(), true));
  }

  @Test
  void testGetUserId() {
    final User user = new User();

    final Map<String, Object> appMetadata = new HashMap<>();
    final String userId = RandomStringUtils.randomAlphabetic(10);
    appMetadata.put("id", userId);
    user.setAppMetadata(appMetadata);
    final String resultUserId = auth0Util.getUserId(user);
    Assertions.assertEquals(userId, resultUserId);
  }

  @Test
  void testAddUser() throws Auth0Exception {
    final Request mockedRequest = Mockito.mock(Request.class);
    Mockito.when(usersEntity.create(Mockito.any())).thenReturn(mockedRequest);
    auth0Util.addUser("example@indeed.com", null, Role.NON_MANAGER.getValue());
    Mockito.verify(mockedRequest, Mockito.times(1)).execute();
  }

  @Nested
  class GetUserByEmailFromAuth0 {


    Request mockedUserRequest;

    @BeforeEach
    void setUp() {
      mockedUserRequest = Mockito.mock(Request.class);
      Mockito.when(usersEntity.listByEmail(Mockito.anyString(), Mockito.any()))
          .thenReturn(mockedUserRequest);
    }

    @Test
    void whenUserMoreThanOne_thenShouldThrow() throws Auth0Exception {
      final List<User> fakeUsersResult = new ArrayList<>(2);
      fakeUsersResult.add(new User());
      fakeUsersResult.add(new User());

      Mockito.when(mockedUserRequest.execute()).thenReturn(fakeUsersResult);
      Assertions.assertThrows(GeneralAuth0Exception.class, () -> {
        auth0Util.getUserByEmailFromAuth0(RandomStringUtils.randomAlphabetic(10));
      });
    }

    @Test
    void whenHasOneUser_thenShouldSuccess() throws Auth0Exception {
      final List<User> fakeUsersResult = new ArrayList<>(1);
      fakeUsersResult.add(new User());

      Mockito.when(mockedUserRequest.execute()).thenReturn(fakeUsersResult);
      Assertions.assertDoesNotThrow(() -> {
        auth0Util.getUserByEmailFromAuth0(RandomStringUtils.randomAlphabetic(10));
      });
    }
  }

  @Nested
  class Login {

    private final static String email = "test@teat.com";
    private final static String password = "Password^34";

    @Nested
    class whenFailed {

      @Test
      void thenThrowException() {
        Assertions.assertThrows(GeneralException.class, () -> auth0Util.login(email, password));
      }
    }
  }
}
