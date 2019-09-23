package shamu.company.utils;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.Permission;
import com.auth0.json.mgmt.PermissionsPage;
import com.auth0.json.mgmt.Role;
import com.auth0.json.mgmt.RolesPage;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.AuthRequest;
import com.auth0.net.Request;
import io.micrometer.core.instrument.util.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import shamu.company.common.exception.AbstractException;
import shamu.company.common.exception.GeneralAuth0Exception;
import shamu.company.common.exception.GeneralException;
import shamu.company.common.exception.TooManyRequestException;

@Component
public class Auth0Util {

  private final Auth0Config auth0Config;

  private final Auth0Manager auth0Manager;

  private static final String managementApi = "managementApi";

  @Autowired
  public Auth0Util(final Auth0Manager auth0Manager,
      final Auth0Config auth0Config) {
    this.auth0Config = auth0Config;
    this.auth0Manager = auth0Manager;
  }

  private AuthAPI getAuthAPI() {
    return auth0Config.getAuthApi();
  }

  private AbstractException handleAuth0Exception(final String message,
                                                     final Auth0Exception e,
                                                     final String api) {
    if ((e.getMessage().contains(String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value())) ||
            e.getMessage().contains(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())) &&
            api.equals("authApi")) {
      return new TooManyRequestException("Too many requests. "
              + "System limits for request are 100 requests per second.",e);
    } else if ((e.getMessage().contains(String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value())) ||
            e.getMessage().contains(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())) &&
            api.equals(managementApi)) {
      return new TooManyRequestException("Too many requests. "
              + "System limits for request are 15 requests per second.",e);
    } else {
      return new GeneralAuth0Exception(message, e);
    }
  }

  public TokenHolder login(final String email, final String password) {
    try {
      final AuthRequest request = getAuthAPI().login(email, password)
          .setAudience(auth0Config.getAudience());
      return request.execute();
    } catch (final Auth0Exception exception) {
      throw handleAuth0Exception(exception.getMessage(), exception,"authApi");
    }
  }

  public boolean isPasswordValid(final String email, final String password) {
    try {
      login(email, password);
      return true;
    } catch (final GeneralException exception) {
      return false;
    }
  }

  public User getUserByEmailFromAuth0(final String email) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    final Request<List<User>> userRequest = manager.users()
        .listByEmail(email, null);
    final List<User> users;
    try {
      users = userRequest.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e.getMessage(), e, managementApi);
    }

    if (users.size() > 1) {
      throw new GeneralAuth0Exception(
          "Multiple account with same email address exist.");
    }

    return CollectionUtils.isEmpty(users) ? null : users.get(0);
  }

  public List<String> getPermissionBy(final String email) {
    final User user = getUserByEmailFromAuth0(email);
    return getPermission(user.getId());
  }

  public List<String> getPermission(final String auth0UserId) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    final Request<PermissionsPage> permissionsPageRequest = manager.users()
        .listPermissions(auth0UserId, null);
    final PermissionsPage permissionsPage;
    try {
      permissionsPage = permissionsPageRequest.execute();
      return permissionsPage.getItems()
          .stream().map(Permission::getName)
          .collect(Collectors.toList());
    } catch (final Auth0Exception e) {
      throw new GeneralAuth0Exception("Get permission error with auth0UserId: " + auth0UserId, e);
    }


  }

  public void updatePassword(final User user, final String newPassword) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    try {
      final User passwordUpdateUser =
          new User();
      passwordUpdateUser.setPassword(newPassword);
      final Request<User> passwordUpdateRequest = manager.users()
          .update(user.getId(), passwordUpdateUser);
      passwordUpdateRequest.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e.getMessage(), e, managementApi);
    }
  }

  public void updateEmail(final String originalEmail, final String newEmail) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    final com.auth0.json.mgmt.users.User authUser = getUserByEmailFromAuth0(originalEmail);
    try {
      final User emailUpdateUser = new User();
      emailUpdateUser.setEmail(newEmail);
      final Request<User> emailUpdateRequest = manager.users()
              .update(authUser.getId(), emailUpdateUser);
      emailUpdateRequest.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e.getMessage(), e, managementApi);
    }
  }

  public void updateUserEmail(final User user, final String newWorkEmail) {
    final ManagementAPI manager = auth0Manager.getManagementApi();

    try {
      final User emailUpdateUser =
          new User();
      emailUpdateUser.setEmail(newWorkEmail);
      emailUpdateUser.setEmailVerified(true);
      final Request<User> passwordUpdateRequest = manager.users()
          .update(user.getId(), emailUpdateUser);
      passwordUpdateRequest.execute();
    } catch (final Auth0Exception e) {
      throw new GeneralAuth0Exception(e.getMessage(), e);
    }


  }

  public void updateVerified(final User user, final boolean verified) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    try {
      final User verifiedUpdate = new User();
      verifiedUpdate.setEmailVerified(verified);
      final Request<User> verifiedRequest = manager.users()
          .update(user.getId(), verifiedUpdate);
      verifiedRequest.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e.getMessage(), e, managementApi);
    }
  }

  public String getUserId(final User user) {
    return (String) user.getAppMetadata().get("id");
  }

  public User addUser(final String email, String password, final String roleName) {
    final User auth0User = new User();
    auth0User.setEmail(email);

    if (StringUtils.isBlank(password)) {
      password = RandomStringUtils.randomAlphabetic(4).toUpperCase()
          + RandomStringUtils.randomAlphabetic(4).toLowerCase()
          + RandomStringUtils.randomNumeric(4);
    }

    auth0User.setPassword(password);
    auth0User.setConnection(auth0Config.getDatabase());
    auth0User.setEmailVerified(false);

    final Map<String, Object> appMetaData = new HashMap<>();
    final String userId = UUID.randomUUID().toString().replaceAll("-", "");
    appMetaData.put("id", userId);
    appMetaData.put("idVerified", true);
    auth0User.setAppMetadata(appMetaData);

    final Map<String, Object> userMetaData = new HashMap<>();
    userMetaData.put("role", roleName);
    auth0User.setUserMetadata(userMetaData);
    auth0User.setEmailVerified(true);

    final ManagementAPI manager = auth0Manager.getManagementApi();
    final Request<User> request = manager.users()
        .create(auth0User);

    try {
      return request.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e.getMessage(), e, managementApi);
    }
  }

  public shamu.company.user.entity.User.Role getUserRole(final String email) {
    try {
      final User user = getUserByEmailFromAuth0(email);
      if (user == null) {
        throw new GeneralAuth0Exception(String.format("Cannot get user with email %s", email));
      }

      final ManagementAPI manager = auth0Manager.getManagementApi();
      final Request<RolesPage> userRoleRequest = manager.users().listRoles(user.getId(), null);
      final RolesPage rolePages = userRoleRequest.execute();
      final List<Role> roles = rolePages.getItems();
      if (roles.size() != 1) {
        throw new GeneralAuth0Exception(String
            .format("User with email %s has wrong role size.", email));
      }

      final Role targetRole = roles.get(0);
      return shamu.company.user.entity.User.Role.valueOf(targetRole.getName());
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e.getMessage(), e, managementApi);
    }
  }

  public void updateRoleWithEmail(final String email, final String targetRoleName) {
    final User user = getUserByEmailFromAuth0(email);
    updateAuthRole(user.getId(), targetRoleName);
  }

  public void updateAuthRole(final String userId, final String updatedRole) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    try {
      final Request<RolesPage> rolesPageRequest = manager.roles().list(null);
      final RolesPage rolesPage = rolesPageRequest.execute();
      final List<String> updatedRoleId = rolesPage.getItems().stream()
          .filter(role -> role.getName().equals(updatedRole))
          .map(Role::getId)
          .collect(Collectors.toList());

      final Request userRolesRequest = manager.users().listRoles(userId, null);
      final RolesPage userRolesPage = (RolesPage) userRolesRequest.execute();
      final List<String> userRolesIds = userRolesPage.getItems().stream()
          .map(Role::getId)
          .collect(Collectors.toList());

      final Request removeRolesRequest = manager.users().removeRoles(userId, userRolesIds);
      removeRolesRequest.execute();

      final Request updateUserRoleRequest = manager.users().addRoles(userId, updatedRoleId);
      updateUserRoleRequest.execute();

    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e.getMessage(), e, managementApi);
    }
  }

  public void deleteUser(final String userId) {
    try {
      final ManagementAPI manager = auth0Manager.getManagementApi();
      final Request deleteUserRequest = manager.users().delete(userId);
      deleteUserRequest.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e.getMessage(), e, managementApi);
    }

  }
}
