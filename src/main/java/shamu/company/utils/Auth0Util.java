package shamu.company.utils;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.PageFilter;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.APIException;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.Permission;
import com.auth0.json.mgmt.PermissionsPage;
import com.auth0.json.mgmt.Role;
import com.auth0.json.mgmt.RolesPage;
import com.auth0.json.mgmt.users.User;
import com.auth0.json.mgmt.users.UsersPage;
import com.auth0.net.AuthRequest;
import com.auth0.net.CustomRequest;
import com.auth0.net.Request;
import com.fasterxml.jackson.core.type.TypeReference;
import io.micrometer.core.instrument.util.StringUtils;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import shamu.company.common.exception.AbstractException;
import shamu.company.common.exception.GeneralAuth0Exception;
import shamu.company.common.exception.TooManyRequestException;

@Component
@Slf4j
public class Auth0Util {

  private static final String MANAGEMENT_API = "managementApi";
  private final Auth0Config auth0Config;
  private final Auth0Manager auth0Manager;
  private static final String MFA_REQUIRED = "mfa_required";
  private final OkHttpClient httpClient = new Builder().build();
  private static final String MFA_ENDPOINT = "http://auth0.com/oauth/grant-type/mfa-otp";
  private static final String MFA_CONTENT_TYPE = "application/x-www-form-urlencoded";

  @Autowired
  public Auth0Util(final Auth0Manager auth0Manager,
      final Auth0Config auth0Config) {
    this.auth0Config = auth0Config;
    this.auth0Manager = auth0Manager;
  }

  private AuthAPI getAuthAPI() {
    return auth0Config.getAuthApi();
  }

  private AbstractException handleAuth0Exception(final Auth0Exception e,
      final String api) {
    if ((e.getMessage().contains(String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value())) ||
        e.getMessage().contains(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())) &&
        api.equals("authApi")) {
      return new TooManyRequestException("Too many requests. "
          + "System limits for request are 100 requests per second.", e);
    } else if ((e.getMessage().contains(String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value())) ||
        e.getMessage().contains(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())) &&
        api.equals(MANAGEMENT_API)) {
      return new TooManyRequestException("Too many requests. "
          + "System limits for request are 15 requests per second.", e);
    } else {
      return new GeneralAuth0Exception(e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public TokenHolder login(final String email, final String password, Function mfaCallback) {
    try {
      final AuthRequest request = getAuthAPI().login(email, password)
          .setAudience(auth0Config.getAudience());
      return request.execute();
    } catch (APIException apiException) {
      if (MFA_REQUIRED.equals(apiException.getError())) {
        if (mfaCallback != null) {
          String mfaToken = (String) apiException.getValue("mfa_token");
          mfaCallback.apply(mfaToken);
        }
        return null;
      }
      throw handleAuth0Exception(apiException, "authApi");
    } catch(final Auth0Exception exception) {
      throw handleAuth0Exception(exception, "authApi");
    } catch (Exception e) {
      throw e;
    }
  }

  @SuppressWarnings("unchecked")
  public TokenHolder validateMfa(String mfaToken, String otp) {

    CustomRequest<TokenHolder> customRequest = new CustomRequest(httpClient,
        String.format("https://%s/oauth/token", auth0Config.getDomain()),
        "POST",
        new TypeReference<TokenHolder>() {});
    customRequest.addHeader("content-type", MFA_CONTENT_TYPE);

    customRequest
        .addParameter("mfa_token", mfaToken)
        .addParameter("otp", otp)
        .addParameter("grant_type", MFA_ENDPOINT)
        .addParameter("client_id", auth0Config.getClientId())
        .addParameter("client_secret", auth0Config.getClientSecret());

    try {
      TokenHolder result = customRequest.execute();
      return result;
    } catch (Auth0Exception e) {
      handleAuth0Exception(e, "authApi");
    }
    return null;
  }

  public boolean isPasswordValid(final String email, final String password) {
    try {
      login(email, password, null);
      return true;
    } catch (final GeneralAuth0Exception exception) {
      return false;
    }
  }

  public Boolean existsByEmail(final String email) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    final Request<List<User>> userRequest = manager.users()
        .listByEmail(email, null);
    final List<User> users;
    try {
      users = userRequest.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }

    if (users.size() > 1) {
      throw new GeneralAuth0Exception(
          "Multiple account with same email address exist.");
    }

    return users.size() == 1;
  }

  public User getUserByUserIdFromAuth0(final String userId) {
    if (StringUtils.isEmpty(userId)) {
      return null;
    }
    final ManagementAPI manager = auth0Manager.getManagementApi();
    UserFilter userFilter = new UserFilter();
    userFilter = userFilter
        .withSearchEngine("v3")
        .withQuery(String.format("app_metadata.id:\"%s\"", userId.toLowerCase()));
    final Request<UsersPage> userRequest = manager.users().list(userFilter);

    final UsersPage usersPage;
    try {
      usersPage = userRequest.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }

    final List<User> users = usersPage.getItems();
    if (users.size() > 1) {
      throw new GeneralAuth0Exception(
          "Multiple account with same email address exist.");
    }

    return CollectionUtils.isEmpty(users) ? null : users.get(0);
  }

  public List<String> getPermissionBy(final String userId) {
    final User user = getUserByUserIdFromAuth0(userId);

    final ManagementAPI manager = auth0Manager.getManagementApi();
    final Request<PermissionsPage> permissionsPageRequest = manager.users()
        .listPermissions(user.getId(), new PageFilter().withPage(0, 100));

    final PermissionsPage permissionsPage;
    try {
      permissionsPage = permissionsPageRequest.execute();
      return permissionsPage.getItems()
          .stream().map(Permission::getName)
          .collect(Collectors.toList());
    } catch (final Auth0Exception e) {
      throw new GeneralAuth0Exception("Get permission error with auth0UserId: " + user.getId(), e);
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
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }
  }

  public void updateEmail(final String userId, final String newEmail) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    final com.auth0.json.mgmt.users.User authUser = getUserByUserIdFromAuth0(userId);
    try {
      final User emailUpdateUser = new User();
      emailUpdateUser.setEmail(newEmail);
      final Request<User> emailUpdateRequest = manager.users()
          .update(authUser.getId(), emailUpdateUser);
      emailUpdateRequest.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e, MANAGEMENT_API);
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
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }
  }

  public String getUserId(final User user) {
    final Map<String, Object> appMetadata = user.getAppMetadata();
    if (appMetadata == null || appMetadata.size() == 0) {
      return null;
    }
    return (String) appMetadata.get("id");
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
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }
  }

  public shamu.company.user.entity.User.Role getUserRole(final String userId) {
    try {
      final User user = getUserByUserIdFromAuth0(userId);
      if (user == null) {
        throw new GeneralAuth0Exception(String.format("Cannot get Auth0 user with user id %s", userId));
      }

      final ManagementAPI manager = auth0Manager.getManagementApi();
      final Request<RolesPage> userRoleRequest = manager.users().listRoles(user.getId(), null);
      final RolesPage rolePages = userRoleRequest.execute();
      final List<Role> roles = rolePages.getItems();

      if (roles.isEmpty() || roles.size() > 1) {
        log.error("User has wrong role size with email: " + user.getEmail());
      }
      final Role targetRole = roles.stream()
          .min(Comparator
              .comparingInt(
                  a -> shamu.company.user.entity.User.Role.valueOf(a.getName()).ordinal()))
          .get();

      return shamu.company.user.entity.User.Role.valueOf(targetRole.getName());
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }
  }

  public void updateRoleWithUserId(final String userId, final String targetRoleName) {
    final User user = getUserByUserIdFromAuth0(userId);
    updateAuthRole(user.getId(), targetRoleName);
  }

  public void updateAuthRole(final String auth0UserId, final String updatedRole) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    try {
      final Request<RolesPage> rolesPageRequest = manager.roles().list(null);
      final RolesPage rolesPage = rolesPageRequest.execute();
      final List<String> updatedRoleId = rolesPage.getItems().stream()
          .filter(role -> role.getName().equals(updatedRole))
          .map(Role::getId)
          .collect(Collectors.toList());

      final Request userRolesRequest = manager.users().listRoles(auth0UserId, null);
      final RolesPage userRolesPage = (RolesPage) userRolesRequest.execute();
      final List<String> userRolesIds = userRolesPage.getItems().stream()
          .map(Role::getId)
          .collect(Collectors.toList());

      final Request removeRolesRequest = manager.users().removeRoles(auth0UserId, userRolesIds);
      removeRolesRequest.execute();

      final Request updateUserRoleRequest = manager.users().addRoles(auth0UserId, updatedRoleId);
      updateUserRoleRequest.execute();

    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }
  }

  public void deleteUser(final String auth0UserId) {
    try {
      final ManagementAPI manager = auth0Manager.getManagementApi();
      final Request deleteUserRequest = manager.users().delete(auth0UserId);
      deleteUserRequest.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }

  }

  public String getUserSecret(final String userId) {
    final User user = getUserByUserIdFromAuth0(userId);
    final Map<String, Object> appMetaData = user.getAppMetadata();
    return (String) appMetaData.get("userSecret");
  }
}
