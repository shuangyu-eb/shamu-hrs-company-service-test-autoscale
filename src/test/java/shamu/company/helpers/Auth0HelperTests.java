package shamu.company.helpers;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.JobsEntity;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.RolesEntity;
import com.auth0.client.mgmt.UsersEntity;
import com.auth0.exception.APIException;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.PermissionsPage;
import com.auth0.json.mgmt.RolesPage;
import com.auth0.json.mgmt.users.User;
import com.auth0.json.mgmt.users.UsersPage;
import com.auth0.net.AuthRequest;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import shamu.company.common.exception.GeneralAuth0Exception;
import shamu.company.common.exception.GeneralException;
import shamu.company.common.exception.NonUniqueAuth0ResourceException;
import shamu.company.helpers.auth0.Auth0Config;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.helpers.auth0.Auth0Manager;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserContactInformation;

class Auth0HelperTests {

  private final shamu.company.user.entity.User companyUser = new shamu.company.user.entity.User();
  private final User auth0User = new User();
  private final com.auth0.json.mgmt.Role role = new com.auth0.json.mgmt.Role();
  private final Auth0Exception auth0Exception = new Auth0Exception("authApi");
  @InjectMocks private Auth0Helper auth0Helper;
  @Mock private Auth0Config auth0Config;
  @Mock private Auth0Manager auth0Manager;
  @Mock private ManagementAPI managementAPI;
  @Mock private UsersEntity usersEntity;
  @Mock private RolesEntity rolesEntity;
  @Mock private JobsEntity jobsEntity;
  @Mock private Request mockedRequest;


  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);

    Mockito.when(auth0Manager.getManagementApi()).thenReturn(managementAPI);
    Mockito.when(managementAPI.users()).thenReturn(usersEntity);
    Mockito.when(managementAPI.roles()).thenReturn(rolesEntity);
    Mockito.when(managementAPI.jobs()).thenReturn(jobsEntity);
    Mockito.when(usersEntity.list(Mockito.any())).thenReturn(mockedRequest);

    final UserContactInformation info = new UserContactInformation();
    info.setEmailWork("example@indeed.com");
    companyUser.setId("1");
    companyUser.setUserContactInformation(info);
    auth0User.setId("1");
    role.setName("ADMIN");

    auth0Helper = new Auth0Helper(auth0Manager, auth0Config);
  }

  @Nested
  class Login {
    AuthRequest mockedRequestAudience;
    Map<String, Object> map;

    @BeforeEach
    void initial() {
      AuthAPI authAPI = Mockito.mock(AuthAPI.class);
      AuthRequest mockedRequest = Mockito.mock(AuthRequest.class);
      mockedRequestAudience = Mockito.mock(AuthRequest.class);
      map = new HashMap<>();
      map.put("error","invalid_grant");
      map.put("error_description","description");
      Mockito.when(auth0Config.getAuthApi()).thenReturn(authAPI);
      Mockito.when(authAPI.login(Mockito.anyString(), Mockito.anyString()))
          .thenReturn(mockedRequest);
      Mockito.when(mockedRequest.setAudience(auth0Config.getAudience()))
          .thenReturn(mockedRequestAudience);
    }

    @Test
    void whenAPILoad_thenShouldSuccess() {
      Assertions.assertDoesNotThrow(
          () -> auth0Helper.login("1", RandomStringUtils.randomAlphabetic(10)));
    }

    @Test
    void whenAPILoadButExecuteFail_thenShouldThrowAPIException() throws Auth0Exception {
      Mockito.when(mockedRequestAudience.execute()).thenThrow(new APIException(map, 100));
      Assertions.assertThrows(
          GeneralAuth0Exception.class,
          () -> auth0Helper.login("1", RandomStringUtils.randomAlphabetic(10)));
    }

    @Test
    void whenAPILoadButMFAError_thenShouldThrowAPIException() throws Auth0Exception {
      map.put("error", "mfa_required");
      Mockito.when(mockedRequestAudience.execute()).thenThrow(new APIException(map, 100));
      Assertions.assertDoesNotThrow(
          () -> auth0Helper.login("1", RandomStringUtils.randomAlphabetic(10)));
    }

    @Test
    void whenAPILoadButExecuteFail_thenShouldThrowAuth0Exception() throws Auth0Exception {
      Mockito.when(mockedRequestAudience.execute()).thenThrow(auth0Exception);
      Assertions.assertThrows(
          GeneralAuth0Exception.class,
          () -> auth0Helper.login("1", RandomStringUtils.randomAlphabetic(10)));
    }

}
  @Test
  void whenAPINotLoad_thenShouldThrow() {
    Assertions.assertThrows(
        GeneralException.class,
        () -> auth0Helper.login("1", RandomStringUtils.randomAlphabetic(10)));
  }

  @Test
  void testIsPasswordValid() {
    final AuthAPI authAPI = Mockito.mock(AuthAPI.class);
    final AuthRequest mockedRequest = Mockito.mock(AuthRequest.class);
    final AuthRequest audienceRequest = Mockito.mock(AuthRequest.class);
    Mockito.when(auth0Config.getAuthApi()).thenReturn(authAPI);
    Mockito.when(authAPI.login(Mockito.anyString(),Mockito.anyString())).thenReturn(mockedRequest);
    Mockito.when(mockedRequest.setAudience(auth0Config.getAudience())).thenReturn(audienceRequest);
    Assertions.assertDoesNotThrow(
        () -> auth0Helper.isPasswordValid("1", RandomStringUtils.randomAlphabetic(10)));
  }

  @Test
  void whenUpdatePassword_thenShouldSuccess() {
    final Request mockedRequest = Mockito.mock(Request.class);
    Mockito.when(usersEntity.update(Mockito.any(), Mockito.any(User.class)))
        .thenReturn(mockedRequest);
    Assertions.assertDoesNotThrow(
        () -> auth0Helper.updatePassword(new User(), RandomStringUtils.randomAlphabetic(10)));
  }

  @Test
  void whenUpdatePasswordFail_thenShouldThrow() throws Auth0Exception {
    final Request mockedRequest = Mockito.mock(Request.class);
    Mockito.when(usersEntity.update(Mockito.any(), Mockito.any(User.class)))
        .thenReturn(mockedRequest);
    Mockito.when(mockedRequest.execute()).thenThrow(auth0Exception);
    Assertions.assertThrows(
        GeneralAuth0Exception.class,
        () -> auth0Helper.updatePassword(new User(), RandomStringUtils.randomAlphabetic(10)));
  }

  @Nested
  class testFindByEmail {

    Request mockedRequest;

    @BeforeEach
    void initial() {
      mockedRequest = Mockito.mock(Request.class);
      Mockito.when(usersEntity.listByEmail(Mockito.anyString(), Mockito.any())).thenReturn(mockedRequest);
    }

    @Test
    void whenUserListIsEmpty_thenShouldReturnNull() throws Auth0Exception {
      final List<User> fakeUsersResult = new ArrayList<>(0);
      Mockito.when(mockedRequest.execute()).thenReturn(fakeUsersResult);
      Assertions.assertNull(auth0Helper.findByEmail("1"));
    }

    @Test
    void whenUserListIsNotEmpty_thenShouldSuccess() throws Auth0Exception {
      final List<User> fakeUsersResult = new ArrayList<>(1);
      fakeUsersResult.add(new User());
      Mockito.when(mockedRequest.execute()).thenReturn(fakeUsersResult);
      Assertions.assertNotNull(auth0Helper.findByEmail("1"));
    }

    @Test
    void whenExecuteFail_thenShouldThrow() throws Auth0Exception {
      final List<User> fakeUsersResult = new ArrayList<>(1);
      fakeUsersResult.add(new User());
      Mockito.when(mockedRequest.execute()).thenThrow(auth0Exception);
      Assertions.assertThrows(
          GeneralAuth0Exception.class,
          () -> auth0Helper.findByEmail("1"));
    }
  }

  @Test
  void testExistsByEmail() throws Auth0Exception {
    final Request mockedRequest = Mockito.mock(Request.class);
    Mockito.when(usersEntity.listByEmail(Mockito.anyString(), Mockito.any())).thenReturn(mockedRequest);
    final List<User> fakeUsersResult = new ArrayList<>(1);
    fakeUsersResult.add(new User());
    Mockito.when(mockedRequest.execute()).thenReturn(fakeUsersResult);
    Assertions.assertTrue(auth0Helper.existsByEmail("1"));
  }

  @Nested
  class GetUserId {

    final User user = new User();
    final Map<String, Object> appMetadata = new HashMap<>();

    @Test
    void whenMetadataIsNull_thenShouldReturnNull() {
      user.setAppMetadata(appMetadata);
      Assertions.assertNull(auth0Helper.getUserId(user));
    }

    @Test
    void whenMetadataNotNull_thenShouldReturnString() {
      final String userId = RandomStringUtils.randomAlphabetic(10);
      appMetadata.put("id", userId);
      user.setAppMetadata(appMetadata);
      final String resultUserId = auth0Helper.getUserId(user);
      Assertions.assertEquals(userId, resultUserId);
    }
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
    UsersPage mockUsersPage;
    List<User> fakeUsersResult;

    @BeforeEach
    void setUp() {
      mockedUserRequest = Mockito.mock(Request.class);
      mockUsersPage = Mockito.mock(UsersPage.class);
      Mockito.when(usersEntity.list(Mockito.any())).thenReturn(mockedUserRequest);
    }

    @Test
    void whenUserMoreThanOne_thenShouldThrow() throws Auth0Exception {
      fakeUsersResult = new ArrayList<>(2);
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
    void whenUserIsNull_thenShouldThrow() throws Auth0Exception {
      fakeUsersResult = new ArrayList<>(0);
      Mockito.when(mockUsersPage.getItems()).thenReturn(fakeUsersResult);
      Mockito.when(mockedUserRequest.execute()).thenReturn(mockUsersPage);
      Assertions.assertNull(auth0Helper.getUserByUserIdFromAuth0(RandomStringUtils.randomAlphabetic(10)));
    }

    @Test
    void whenHasOneUser_thenShouldSuccess() throws Auth0Exception {
      fakeUsersResult = new ArrayList<>(1);
      fakeUsersResult.add(new User());
      Mockito.when(mockUsersPage.getItems()).thenReturn(fakeUsersResult);
      Mockito.when(mockedUserRequest.execute()).thenReturn(mockUsersPage);
      Assertions.assertDoesNotThrow(
          () -> {
            auth0Helper.getUserByUserIdFromAuth0(RandomStringUtils.randomAlphabetic(10));
          });
    }

    @Test
    void whenExecuteFail_thenShouldThrow() throws Auth0Exception {
      fakeUsersResult = new ArrayList<>(0);
      Mockito.when(mockUsersPage.getItems()).thenReturn(fakeUsersResult);
      Mockito.when(mockedUserRequest.execute()).thenThrow(auth0Exception);
      Assertions.assertThrows(
          GeneralAuth0Exception.class,
          () -> {auth0Helper.getUserByUserIdFromAuth0(RandomStringUtils.randomAlphabetic(10));}
      );
    }
  }

  @Nested
  class GetAuth0UserByIdWithByEmailFailover {
    Request mockedRequest;
    UsersPage mockUsersPage;
    List<User> fakeUsersResult;
    @BeforeEach
    void initial() throws Auth0Exception {
      mockedRequest = Mockito.mock(Request.class);
      mockUsersPage = Mockito.mock(UsersPage.class);
      Mockito.when(usersEntity.list(Mockito.any())).thenReturn(mockedRequest);
      Mockito.when(mockedRequest.execute()).thenReturn(mockUsersPage);
    }

    @Test
    void whenUserIsNull_thenShouldFindByEmail() throws Auth0Exception {
      fakeUsersResult = new ArrayList<>(0);
      Mockito.when(mockUsersPage.getItems()).thenReturn(fakeUsersResult);
      final Request emailRequest = Mockito.mock(Request.class);
      final List<User> users = new ArrayList<>(0);
      Mockito.when(usersEntity.listByEmail(Mockito.anyString(), Mockito.any())).thenReturn(emailRequest);
      Mockito.when(emailRequest.execute()).thenReturn(users);
      Assertions.assertDoesNotThrow(
          () -> {
            auth0Helper.getAuth0UserByIdWithByEmailFailover(companyUser.getId(),
                companyUser.getUserContactInformation().getEmailWork());
          });
    }

    @Test
    void whenUserMoreThanOne_thenShouldThrow() {
      fakeUsersResult = new ArrayList<>(2);
      fakeUsersResult.add(auth0User);
      fakeUsersResult.add(new User());
      Mockito.when(mockUsersPage.getItems()).thenReturn(fakeUsersResult);
      Assertions.assertDoesNotThrow(
          () -> {
            auth0Helper.getAuth0UserByIdWithByEmailFailover(companyUser.getId(),
                companyUser.getUserContactInformation().getEmailWork());
          });
    }

    @Test
    void whenGetPermissionExecute_thenShouldSuccess() throws Auth0Exception {
      final Request permissionRequest = Mockito.mock(Request.class);
      PermissionsPage permissionsPage = Mockito.mock(PermissionsPage.class);
      fakeUsersResult = new ArrayList<>(1);
      fakeUsersResult.add(auth0User);
      Mockito.when(mockUsersPage.getItems()).thenReturn(fakeUsersResult);
      Mockito.when(usersEntity.listPermissions(Mockito.anyString(),Mockito.any())).thenReturn(permissionRequest);
      Mockito.when(permissionRequest.execute()).thenReturn(permissionsPage);
      Assertions.assertDoesNotThrow(
          () -> {
            auth0Helper.getPermissionBy(companyUser);
          });
    }

    @Test
    void whenGetPermissionExecuteFail_thenShouldThrow() throws Auth0Exception {
      final Request permissionRequest = Mockito.mock(Request.class);
      fakeUsersResult = new ArrayList<>(1);
      fakeUsersResult.add(auth0User);
      Mockito.when(mockUsersPage.getItems()).thenReturn(fakeUsersResult);
      Mockito.when(usersEntity.listPermissions(Mockito.anyString(),Mockito.any())).thenReturn(permissionRequest);
      Mockito.when(permissionRequest.execute()).thenThrow(auth0Exception);
      Assertions.assertThrows(
          GeneralAuth0Exception.class,
          () -> {
            auth0Helper.getPermissionBy(companyUser);
          });
    }
  }

  @Nested
  class UpdateEmail {

    Request request;
    PermissionsPage permissionsPage;
    @BeforeEach
    void init() throws Auth0Exception {
      request = Mockito.mock(Request.class);
      permissionsPage = Mockito.mock(PermissionsPage.class);
      final Request mockedRequest = Mockito.mock(Request.class);
      final UsersPage mockUsersPage = Mockito.mock(UsersPage.class);
      final List<User> fakeUsersResult = new ArrayList<>(1);
      fakeUsersResult.add(auth0User);
      Mockito.when(usersEntity.list(Mockito.any())).thenReturn(mockedRequest);
      Mockito.when(mockUsersPage.getItems()).thenReturn(fakeUsersResult);
      Mockito.when(mockedRequest.execute()).thenReturn(mockUsersPage);
      Mockito.when(usersEntity.update(Mockito.anyString(),Mockito.any())).thenReturn(request);
    }

    @Test
    void whenUpdateEmail_thenShouldSuccess() throws Auth0Exception {
      Mockito.when(request.execute()).thenReturn(permissionsPage);
      Assertions.assertDoesNotThrow(
          () -> {
            auth0Helper.updateEmail(companyUser,"newexample@indeed.com");
          });
    }
    @Test
    void whenUpdateEmailFail_thenShouldThrow() throws Auth0Exception {
      Mockito.when(request.execute()).thenThrow(auth0Exception);
      Assertions.assertThrows(
          GeneralAuth0Exception.class,
          () -> {
            auth0Helper.updateEmail(companyUser,"newexample@indeed.com");
          });
    }
    @Test
    void whenUpdateUserEmail_thenShouldSuccess() {
      Mockito.when(usersEntity.update(Mockito.anyString(),Mockito.any())).thenReturn(request);
      Assertions.assertDoesNotThrow(
          () -> {
            auth0Helper.updateUserEmail(auth0User,"newexample@indeed.com");
          });
    }

    @Test
    void whenUpdateUserEmailFail_thenShouldThrow() throws Auth0Exception {
      Mockito.when(usersEntity.update(Mockito.anyString(),Mockito.any())).thenReturn(request);
      Mockito.when(request.execute()).thenThrow(auth0Exception);
      Assertions.assertThrows(
          GeneralAuth0Exception.class,
          () -> {
            auth0Helper.updateUserEmail(auth0User,"newexample@indeed.com");
          });
    }
    @Test
    void whenUpdateVerified_thenShouldSuccess() {
      Mockito.when(usersEntity.update(Mockito.any(), Mockito.any(User.class)))
          .thenReturn(request);
      Assertions.assertDoesNotThrow(() -> auth0Helper.updateVerified(new User(), true));
    }

    @Test
    void whenUpdateVerifiedFail_thenShouldThrow() throws Auth0Exception {
      Mockito.when(usersEntity.update(Mockito.any(), Mockito.any(User.class)))
          .thenReturn(request);
      Mockito.when(request.execute()).thenThrow(auth0Exception);
      Assertions.assertThrows(
          GeneralAuth0Exception.class,
          () -> auth0Helper.updateVerified(new User(), true));
    }
  }

  @Nested
  class GetUserRole {
    Request request;
    Request userRoleRequest;
    PermissionsPage permissionsPage;
    UsersPage mockUsersPage;
    RolesPage rolesPage;
    List<User> fakeUsersResult;
    List<com.auth0.json.mgmt.Role> roles;

    @BeforeEach
    void initial() throws Auth0Exception {
      request = Mockito.mock(Request.class);
      userRoleRequest = Mockito.mock(Request.class);
      permissionsPage = Mockito.mock(PermissionsPage.class);
      mockUsersPage = Mockito.mock(UsersPage.class);
      rolesPage = Mockito.mock(RolesPage.class);
      fakeUsersResult = new ArrayList<>(1);
      fakeUsersResult.add(auth0User);
      Mockito.when(usersEntity.list(Mockito.any())).thenReturn(mockedRequest);
      Mockito.when(mockUsersPage.getItems()).thenReturn(fakeUsersResult);
      Mockito.when(mockedRequest.execute()).thenReturn(mockUsersPage);
      Mockito.when(usersEntity.update(Mockito.anyString(),Mockito.any())).thenReturn(request);
      Mockito.when(request.execute()).thenReturn(permissionsPage);
      Mockito.when(usersEntity.listRoles(Mockito.anyString(),Mockito.any())).thenReturn(userRoleRequest);
      Mockito.when(userRoleRequest.execute()).thenReturn(rolesPage);
    }

    @Test
    void whenRolesIsEmpty_thenShouldThrow() {
      roles = new ArrayList<>(0);
      Assertions.assertThrows(GeneralAuth0Exception.class,() -> auth0Helper.getUserRole(companyUser));
    }

    @Test
    void whenRolesIsNotEmpty_thenShouldSuccess() {
      roles = new ArrayList<>(1);
      roles.add(role);
      Mockito.when(rolesPage.getItems()).thenReturn(roles);
      Assertions.assertDoesNotThrow(
          () -> {
            auth0Helper.getUserRole(companyUser);
          });
    }

    @Test
    void testUpdateRole() throws Auth0Exception {
      roles = new ArrayList<>(1);
      roles.add(role);
      Mockito.when(rolesPage.getItems()).thenReturn(roles);
      final Request userRolesRequest = Mockito.mock(Request.class);
      final Request rolesPageRequest = Mockito.mock(Request.class);
      final Request removeRolesRequest = Mockito.mock(Request.class);
      final Request updateUserRoleRequest = Mockito.mock(Request.class);
      final RolesPage rolesPage = Mockito.mock(RolesPage.class);
      final RolesPage userRolesPage = Mockito.mock(RolesPage.class);
      Mockito.when(rolesEntity.list(Mockito.any())).thenReturn(userRolesRequest);
      Mockito.when(userRolesRequest.execute()).thenReturn(rolesPage);
      Mockito.when(usersEntity.listRoles(Mockito.anyString(), Mockito.any())).thenReturn(rolesPageRequest);
      Mockito.when(rolesPageRequest.execute()).thenReturn(userRolesPage);
      Mockito.when(usersEntity.removeRoles(Mockito.anyString(), Mockito.anyList())).thenReturn(removeRolesRequest);
      Mockito.when(usersEntity.addRoles(Mockito.anyString(), Mockito.anyList())).thenReturn(updateUserRoleRequest);
      auth0Helper.updateRole(companyUser,Role.EMPLOYEE.getValue());
      Mockito.verify(removeRolesRequest, Mockito.times(1)).execute();
      Mockito.verify(updateUserRoleRequest, Mockito.times(1)).execute();
    }
  }

  @Test
  void testValidateMfa() {
    Assertions.assertThrows(GeneralAuth0Exception.class,() -> auth0Helper.validateMfa("1","1"));
  }


  @Test
  void testUpdateAuthRole() throws Auth0Exception {
    final Request userRolesRequest = Mockito.mock(Request.class);
    final Request rolesPageRequest = Mockito.mock(Request.class);
    final Request removeRolesRequest = Mockito.mock(Request.class);
    final Request updateUserRoleRequest = Mockito.mock(Request.class);
    final RolesPage rolesPage = Mockito.mock(RolesPage.class);
    final RolesPage userRolesPage = Mockito.mock(RolesPage.class);
    Mockito.when(rolesEntity.list(Mockito.any())).thenReturn(userRolesRequest);
    Mockito.when(userRolesRequest.execute()).thenReturn(rolesPage);
    Mockito.when(usersEntity.listRoles(Mockito.anyString(), Mockito.any())).thenReturn(rolesPageRequest);
    Mockito.when(rolesPageRequest.execute()).thenReturn(userRolesPage);
    Mockito.when(usersEntity.removeRoles(Mockito.anyString(), Mockito.anyList())).thenReturn(removeRolesRequest);
    Mockito.when(usersEntity.addRoles(Mockito.anyString(), Mockito.anyList())).thenReturn(updateUserRoleRequest);
    auth0Helper.updateAuthRole(auth0User.getId(),Role.EMPLOYEE.getValue());
    Mockito.verify(removeRolesRequest, Mockito.times(1)).execute();
    Mockito.verify(updateUserRoleRequest, Mockito.times(1)).execute();
  }

  @Test
  void testDeleteUser() throws Auth0Exception {
    final Request deleteRequest = Mockito.mock(Request.class);
    Mockito.when(usersEntity.delete(Mockito.anyString())).thenReturn(deleteRequest);
    auth0Helper.deleteUser(auth0User.getId());
    Mockito.verify(deleteRequest, Mockito.times(1)).execute();
  }

  @Test
  void testGetUserSecret() throws Auth0Exception {
    final Request permissionRequest = Mockito.mock(Request.class);
    final PermissionsPage permissionsPage = Mockito.mock(PermissionsPage.class);
    final UsersPage mockUsersPage = Mockito.mock(UsersPage.class);
    final List<User> fakeUsersResult = new ArrayList<>(1);
    final Map<String, Object> map = new HashMap<String,Object>();
    map.put("userSecret","1");
    auth0User.setAppMetadata(map);
    fakeUsersResult.add(auth0User);
    Mockito.when(mockUsersPage.getItems()).thenReturn(fakeUsersResult);
    Mockito.when(mockedRequest.execute()).thenReturn(mockUsersPage);
    Mockito.when(usersEntity.listPermissions(Mockito.anyString(),Mockito.any())).thenReturn(permissionRequest);
    Mockito.when(permissionRequest.execute()).thenReturn(permissionsPage);
    Assertions.assertDoesNotThrow(
        () -> {
          auth0Helper.getUserSecret(companyUser);
        });
  }

  @Test
  void testSendVerificationEmail() {
    final Request sendEmailRequest = Mockito.mock(Request.class);

    Mockito.when(jobsEntity.sendVerificationEmail(auth0User.getId(), auth0Config.getClientId())).thenReturn(sendEmailRequest);
    Assertions.assertDoesNotThrow(
        () -> {
          auth0Helper.sendVerificationEmail(auth0User.getId());
        });
  }
}
