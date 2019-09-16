package shamu.company.utils;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
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
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import shamu.company.common.exception.GeneralAuth0Exception;
import shamu.company.common.exception.GeneralException;

@Component
public class Auth0Util {

  private final Auth0Config auth0Config;

  private final Auth0Manager auth0Manager;

  @Autowired
  public Auth0Util(final Auth0Manager auth0Manager,
      final Auth0Config auth0Config) {
    this.auth0Config = auth0Config;
    this.auth0Manager = auth0Manager;
  }
  private AuthAPI getAuthAPI() {
    return auth0Config.getAuthApi();
  }

  public TokenHolder login(final String email, final String password) {
    try {
      final AuthRequest request = getAuthAPI().login(email, password)
          .setAudience(auth0Config.getAudience());
      return request.execute();
    } catch (final Auth0Exception exception) {
      throw new GeneralException(exception.getMessage(), exception);
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
      throw new GeneralAuth0Exception(e.getMessage(), e);
    }

    if (users.size() > 1) {
      throw new GeneralAuth0Exception(
          "Multiple account with same email address exist.");
    }

    return CollectionUtils.isEmpty(users) ? null : users.get(0);
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
      throw new GeneralAuth0Exception(e.getMessage(), e);
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
      throw new GeneralAuth0Exception(e.getMessage(), e);
    }
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
      throw new GeneralAuth0Exception(e.getMessage(), e);
    }
  }

  public void deleteUser(final String userId) {
    try {
      final ManagementAPI manager = auth0Manager.getManagementApi();
      final Request deleteUserRequest = manager.users().delete(userId);
      deleteUserRequest.execute();
    } catch (final Auth0Exception e) {
      throw new GeneralException(e.getMessage(), e);
    }

  }
}
