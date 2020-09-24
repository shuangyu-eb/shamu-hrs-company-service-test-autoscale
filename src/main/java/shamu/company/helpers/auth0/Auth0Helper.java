package shamu.company.helpers.auth0;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.PageFilter;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.APIException;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.CreatedUser;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.Permission;
import com.auth0.json.mgmt.PermissionsPage;
import com.auth0.json.mgmt.Role;
import com.auth0.json.mgmt.RolesPage;
import com.auth0.json.mgmt.jobs.Job;
import com.auth0.json.mgmt.users.User;
import com.auth0.json.mgmt.users.UsersPage;
import com.auth0.net.AuthRequest;
import com.auth0.net.CustomRequest;
import com.auth0.net.Request;
import com.auth0.net.SignUpRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import shamu.company.common.exception.errormapping.AlreadyExistsException;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.common.exception.errormapping.TooManyRequestException;
import shamu.company.helpers.auth0.exception.EmailUpdateFailedException;
import shamu.company.helpers.auth0.exception.GeneralAuth0Exception;
import shamu.company.helpers.auth0.exception.LoginFailedException;
import shamu.company.helpers.auth0.exception.NonUniqueAuth0ResourceException;
import shamu.company.helpers.auth0.exception.PermissionGetFailedException;
import shamu.company.helpers.auth0.exception.SignUpFailedException;
import shamu.company.helpers.auth0.exception.errormapping.VerificationEmailSendFailedException;
import shamu.company.sentry.SentryLogger;
import shamu.company.utils.UuidUtil;

@Component
public class Auth0Helper {

  private static final SentryLogger log = new SentryLogger(Auth0Helper.class);

  private static final String MANAGEMENT_API = "managementApi";
  private static final String AUTH_API = "authApi";
  private static final String MFA_ENDPOINT = "http://auth0.com/oauth/grant-type/mfa-otp";
  private static final String MFA_CONTENT_TYPE = "application/x-www-form-urlencoded";
  private static final String MFA_REQUIRED = "mfa_required";
  private static final String INVALID_GRANT = "invalid_grant";
  private final Auth0Config auth0Config;
  private final Auth0Manager auth0Manager;
  private final OkHttpClient httpClient = new Builder().build();
  private static final String USER_SECRET = "userSecret";
  public static final String COMPANY_ID = "companyId";
  public static final String USER_ID = "id";
  @Value("${auth0.database}")
  private  String connection;

  @Autowired
  public Auth0Helper(final Auth0Manager auth0Manager, final Auth0Config auth0Config) {
    this.auth0Config = auth0Config;
    this.auth0Manager = auth0Manager;
  }

  private AuthAPI getAuthApi() {
    return auth0Config.getAuthApi();
  }

  private RuntimeException handleAuth0Exception(final Auth0Exception e, final String api) {
    if ((e.getMessage().contains(String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()))
            || e.getMessage().contains(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase()))
        && AUTH_API.equals(api)) {
      return new TooManyRequestException(
          "Too many requests. " + "System limits for request are 100 requests per second.", e);
    } else if ((e.getMessage().contains(String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()))
            || e.getMessage().contains(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase()))
        && api.equals(MANAGEMENT_API)) {
      return new TooManyRequestException(
          "Too many requests. " + "System limits for request are 15 requests per second.", e);
    } else if (e instanceof APIException
        && StringUtils.equalsIgnoreCase(((APIException) e).getError(), INVALID_GRANT)) {
      return new GeneralAuth0Exception(((APIException) e).getDescription(), e);
    }
    return new GeneralAuth0Exception(e.getMessage(), e);
  }

  public void login(final String email, final String password) {
    try {
      final AuthRequest request =
          getAuthApi().login(email, password).setAudience(auth0Config.getAudience());
      request.execute();
    } catch (final APIException apiException) {
      if (MFA_REQUIRED.equals(apiException.getError())) {
        log.info(String.format("MFA required when validating password of account %s.", email));
        return;
      }
      throw handleAuth0Exception(apiException, AUTH_API);
    } catch (final Auth0Exception exception) {
      throw handleAuth0Exception(exception, AUTH_API);
    } catch (final Exception e) {
      throw new LoginFailedException("LoginFailedException:" + e.getMessage(), e);
    }
  }

  public CreatedUser signUp(final String email, final String password) {
    final SignUpRequest request = getAuthApi().signUp(email, password, auth0Config.getDatabase());
    try {
      return request.execute();
    } catch (final APIException apiException) {
      if ("user_exists".equals(apiException.getError())) {
        throw new AlreadyExistsException("The email already exists.", email);
      }
      throw handleAuth0Exception(apiException, AUTH_API);
    } catch (final Auth0Exception exception) {
      throw handleAuth0Exception(exception, AUTH_API);
    } catch (final Exception e) {
      throw new SignUpFailedException("SignUpFailedException:" + e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public TokenHolder validateMfa(final String mfaToken, final String otp) {

    final CustomRequest<TokenHolder> customRequest =
        new CustomRequest(
            httpClient,
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
      return customRequest.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e, AUTH_API);
    }
  }

  public boolean isPasswordValid(final String email, final String password) {
    try {
      login(email, password);
      return true;
    } catch (final GeneralAuth0Exception | LoginFailedException e) {
      return false;
    }
  }

  public User findByEmail(final String emailRaw) {
    final String email = emailRaw.toLowerCase();
    final ManagementAPI manager = auth0Manager.getManagementApi();
    final Request<List<User>> userRequest = manager.users().listByEmail(email, null);
    final List<User> users;
    try {
      users = userRequest.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }

    if (users.isEmpty()) {
      log.info("Unable to find a user with email " + email + ".");
      return null;
    }

    return users.get(0);
  }

  public boolean existsByEmail(final String email) {
    return findByEmail(email) != null;
  }

  public User getUserByUserIdFromAuth0(final String userId) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    UserFilter userFilter = new UserFilter();
    userFilter =
        userFilter
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
      throw new NonUniqueAuth0ResourceException("Multiple Auth0 users with the same id exist.");
    }

    if (users.isEmpty()) {
      log.info("No Auth0 user with id " + userId + ".");
      return null;
    }

    return users.get(0);
  }

  public User getAuth0UserByIdWithByEmailFailover(final String userId, final String email) {
    final User emptyUser = new User();
    try {
      User user = getUserByUserIdFromAuth0(userId);
      if (user == null) {
        log.info("Unable to find a user with id " + userId + ". Attempting with email.");
        user = findByEmail(email);
      }
      return user == null ? emptyUser : user;
    } catch (final NonUniqueAuth0ResourceException e) {
      log.error("User id " + userId + " matches more than 1 Auth0 user.", e);
      return emptyUser;
    }
  }

  public List<String> getPermissionBy(final shamu.company.user.entity.User user) {
    final String userWorkEmail = user.getUserContactInformation().getEmailWork();
    final User auth0User = getAuth0UserByIdWithByEmailFailover(user.getId(), userWorkEmail);

    final ManagementAPI manager = auth0Manager.getManagementApi();
    final Request<PermissionsPage> permissionsPageRequest =
        manager.users().listPermissions(auth0User.getId(), new PageFilter().withPage(0, 100));

    final PermissionsPage permissionsPage;
    try {
      permissionsPage = permissionsPageRequest.execute();
      return permissionsPage.getItems().stream()
          .map(Permission::getName)
          .collect(Collectors.toList());
    } catch (final Auth0Exception e) {
      throw new PermissionGetFailedException(
          "Get permission error with auth0UserId: " + auth0User.getId(), e);
    }
  }

  public void updatePassword(final User user, final String newPassword) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    try {
      final User passwordUpdateUser = new User();
      passwordUpdateUser.setPassword(newPassword);
      final Request<User> passwordUpdateRequest =
          manager.users().update(user.getId(), passwordUpdateUser);
      passwordUpdateRequest.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }
  }

  public void updateEmail(final shamu.company.user.entity.User user, final String newEmail) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    final String userWorkEmail = user.getUserContactInformation().getEmailWork();
    final User authUser = getAuth0UserByIdWithByEmailFailover(user.getId(), userWorkEmail);
    try {
      final User emailUpdateUser = new User();
      emailUpdateUser.setEmail(newEmail);
      final Request<User> emailUpdateRequest =
          manager.users().update(authUser.getId(), emailUpdateUser);
      emailUpdateRequest.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }
  }

  public void updateUserEmail(final User user, final String newWorkEmail) {
    final ManagementAPI manager = auth0Manager.getManagementApi();

    try {
      final User emailUpdateUser = new User();
      emailUpdateUser.setEmail(newWorkEmail);
      emailUpdateUser.setEmailVerified(true);
      final Request<User> passwordUpdateRequest =
          manager.users().update(user.getId(), emailUpdateUser);
      passwordUpdateRequest.execute();
    } catch (final Auth0Exception e) {
      throw new EmailUpdateFailedException(e.getMessage(), e);
    }
  }

  public void updateVerified(final User user, final boolean verified) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    try {
      final User verifiedUpdate = new User();
      verifiedUpdate.setEmailVerified(verified);
      final Request<User> verifiedRequest = manager.users().update(user.getId(), verifiedUpdate);
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
    return (String) appMetadata.get(USER_ID);
  }

  public User updateAuthUserAppMetaData(final String userId) {
    final boolean isIndeedConnection = Auth0ConnectionEnum.INDEED.getValue().equals(connection);
    final String prefix = isIndeedConnection ? "" : "auth0|";
    final String newUserId = prefix + userId;
    final User user = new User();

    final String userIdInDatabase = UuidUtil.getUuidString();
    final String userSecret = generateUserSecret(userIdInDatabase);
    final Map<String, Object> appMetaData = new HashMap<>();
    appMetaData.put(USER_ID, userIdInDatabase);
    appMetaData.put(USER_SECRET, userSecret);
    appMetaData.put(COMPANY_ID, UuidUtil.getUuidString().toUpperCase());
    if(isIndeedConnection) {
      appMetaData.put("verification_email_sent", true);
    } else {
      appMetaData.put("idVerified", true);
    }
    user.setAppMetadata(appMetaData);
    final ManagementAPI manager = auth0Manager.getManagementApi();
    final Request<User> request = manager.users().update(newUserId, user);

    try {
      return request.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }
  }

  public User addUser(final String email, String password, final String roleName) {
    final User auth0User = new User();
    auth0User.setEmail(email);

    if (StringUtils.isBlank(password)) {
      final SecureRandom secureRandom = new SecureRandom();
      password =
          RandomStringUtils.randomAlphabetic(4).toUpperCase()
              + RandomStringUtils.randomAlphabetic(4).toLowerCase()
              + secureRandom.nextInt(10)
              + secureRandom.nextInt(10)
              + secureRandom.nextInt(10)
              + secureRandom.nextInt(10);
    }

    auth0User.setPassword(password);
    auth0User.setConnection(auth0Config.getDatabase());
    auth0User.setEmailVerified(false);

    final Map<String, Object> appMetaData = new HashMap<>();
    final String userId = UuidUtil.getUuidString();
    final String userSecret = generateUserSecret(userId);
    appMetaData.put(USER_ID, userId);
    appMetaData.put("idVerified", true);
    appMetaData.put("role", roleName);
    appMetaData.put(USER_SECRET, userSecret);
    appMetaData.put(COMPANY_ID, TenantContext.getCurrentTenant());
    auth0User.setAppMetadata(appMetaData);

    auth0User.setEmailVerified(true);

    final ManagementAPI manager = auth0Manager.getManagementApi();
    final Request<User> request = manager.users().create(auth0User);

    try {
      return request.execute();
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }
  }

  public shamu.company.user.entity.User.Role getUserRole(
      final shamu.company.user.entity.User user) {
    final String userWorkEmail = user.getUserContactInformation().getEmailWork();
    final User auth0User = getAuth0UserByIdWithByEmailFailover(user.getId(), userWorkEmail);
    if (auth0User == null) {
      throw new ResourceNotFoundException(
          String.format("Auth0 user with id %s not found", user.getId()),
          user.getId(),
          "auth0 user");
    }
    return getUserRole(auth0User);
  }

  public shamu.company.user.entity.User.Role getUserRoleByUserId(final String id) {
    final User auth0User = getUserByUserIdFromAuth0(id);
    if (auth0User == null) {
      throw new ResourceNotFoundException(
          String.format("Auth0 user with id %s not found", id), id, "auth0 user");
    }
    return getUserRole(auth0User);
  }

  private shamu.company.user.entity.User.Role getUserRole(final User auth0User) {
    try {
      final ManagementAPI manager = auth0Manager.getManagementApi();
      final Request<RolesPage> userRoleRequest = manager.users().listRoles(auth0User.getId(), null);
      final RolesPage rolePages = userRoleRequest.execute();
      final List<Role> roles = rolePages.getItems();

      if (roles.size() != 1) {
        log.error("User has wrong role size with email: " + auth0User.getEmail());
      }
      final Role targetRole =
          roles.stream()
              .min(
                  Comparator.comparingInt(
                      a -> shamu.company.user.entity.User.Role.valueOf(a.getName()).ordinal()))
              .orElseThrow(() -> new Auth0Exception("Role is empty"));

      return shamu.company.user.entity.User.Role.valueOf(targetRole.getName());
    } catch (final Auth0Exception e) {
      throw handleAuth0Exception(e, MANAGEMENT_API);
    }
  }

  public void updateRole(final shamu.company.user.entity.User user, final String targetRoleName) {
    final String userWorkEmail = user.getUserContactInformation().getEmailWork();
    final User auth0User = getAuth0UserByIdWithByEmailFailover(user.getId(), userWorkEmail);
    updateAuthRole(auth0User.getId(), targetRoleName);
  }

  public void updateAuthRole(final String auth0UserId, final String updatedRole) {
    final ManagementAPI manager = auth0Manager.getManagementApi();
    try {
      final Request<RolesPage> rolesPageRequest = manager.roles().list(null);
      final RolesPage rolesPage = rolesPageRequest.execute();
      final List<String> updatedRoleId =
          rolesPage.getItems().stream()
              .filter(role -> role.getName().equals(updatedRole))
              .map(Role::getId)
              .collect(Collectors.toList());

      final Request userRolesRequest = manager.users().listRoles(auth0UserId, null);
      final RolesPage userRolesPage = (RolesPage) userRolesRequest.execute();
      final List<String> userRolesIds =
          userRolesPage.getItems().stream().map(Role::getId).collect(Collectors.toList());

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

  public String getUserSecret(final shamu.company.user.entity.User user) {
    final String userWorkEmail = user.getUserContactInformation().getEmailWork();
    final User auth0User = getAuth0UserByIdWithByEmailFailover(user.getId(), userWorkEmail);
    final Map<String, Object> appMetaData = auth0User.getAppMetadata();
    return (String) appMetaData.get(USER_SECRET);
  }

  public void sendVerificationEmail(final String authUserId) {
    final ManagementAPI managementApi = auth0Manager.getManagementApi();
    final Request<Job> sendVerificationEmail =
        managementApi.jobs().sendVerificationEmail(authUserId, auth0Config.getClientId());
    try {
      sendVerificationEmail.execute();
    } catch (final Auth0Exception e) {
      throw new VerificationEmailSendFailedException(e.getMessage(), e);
    }
  }

  public String generateUserSecret(final String userId) {
    final String userSalt = BCrypt.gensalt();
    return BCrypt.hashpw(userId, userSalt);
  }

  public boolean isIndeedEnvironment() {
    return Auth0ConnectionEnum.INDEED.getValue().equals(connection);
  }
}
