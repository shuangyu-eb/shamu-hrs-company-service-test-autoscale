package shamu.company.crypto;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.stereotype.Component;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserService;

@Component
public class Encryptor {

  private static final Charset charset = StandardCharsets.ISO_8859_1;
  private final String indeedHash;
  private final UserService userService;
  private final Auth0Helper auth0Helper;
  private final SecretHashRepository secretHashRepository;

  @Autowired
  Encryptor(
      final @Value("${crypto.hash}") String indeedHash,
      final UserService userService,
      final Auth0Helper auth0Helper,
      final SecretHashRepository secretHashRepository) {
    this.indeedHash = indeedHash;
    this.userService = userService;
    this.auth0Helper = auth0Helper;
    this.secretHashRepository = secretHashRepository;
  }

  private static String encrypt(final String value, final BytesEncryptor encryptor) {
    return Base64.getEncoder().encodeToString(encryptor.encrypt(value.getBytes(charset)));
  }

  public String encrypt(final String userId, final String value) {
    return encrypt(value, getEncryptor(userId));
  }

  public String encrypt(final User user, final String value) {
    return encrypt(value, getEncryptor(user));
  }

  private String decrypt(final String userId, final String value) {
    final User user = userService.findActiveUserById(userId);
    return decrypt(user, value);
  }

  private String decrypt(final User user, final String value) {
    return new String(getEncryptor(user).decrypt(Base64.getDecoder().decode(value)), charset);
  }

  String decrypt(final String id, final Class entityClass, final String value) {
    try {
      if (entityClass == User.class) {
        return decrypt(id, value);
      } else if (entityClass == UserPersonalInformation.class) {
        final User user = userService.findUserByUserPersonalInformationId(id);
        return decrypt(user, value);
      } else if (entityClass == BenefitPlanDependent.class) {
        return decrypt(id, value);
      }
    } catch (final Exception e) {
      // for the unencoded value
      return value;
    }
    return null;
  }

  private BytesEncryptor getEncryptor(final User user) {
    return Encryptors.stronger(String.valueOf(getHashCode(user)), user.getSalt());
  }

  private BytesEncryptor getEncryptor(final String userId) {
    final User user = userService.findById(userId);
    return Encryptors.stronger(String.valueOf(getHashCode(user)), user.getSalt());
  }

  private int getHashCode(final User user) {
    final String companyHash = getCompanyHash();

    String userHash = "";
    if(auth0Helper.isIndeedEnvironment()) {
      if (StringUtils.isEmpty(user.getHash())) {
        userHash = getUserHash(user);
      } else {
        userHash = user.getHash();
      }
    } else {
      userHash = getUserHash(user);
    }

    return Objects.hash(indeedHash, companyHash, userHash);
  }

  private String getUserHash(final User user) {
    return auth0Helper.getUserSecret(user);
  }

  private String getCompanyHash() {
    return secretHashRepository.getCompanySecretByCompanyId(TenantContext.getCurrentTenant());
  }
}
