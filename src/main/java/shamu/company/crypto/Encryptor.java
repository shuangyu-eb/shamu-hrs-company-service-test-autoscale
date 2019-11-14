package shamu.company.crypto;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.stereotype.Component;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.SecretHashRepository;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.repository.UserRepository;
import shamu.company.utils.Auth0Util;

@Component
public class Encryptor {

  private static final Charset charset = StandardCharsets.ISO_8859_1;
  private final String indeedHash;
  private final UserRepository userRepository;
  private final Auth0Util auth0Util;
  private final SecretHashRepository secretHashRepository;
  private final String salt;

  @Autowired
  Encryptor(final @Value("crypto.hash") String indeedHash,
      final @Value("crypto.salt") String salt,
      final UserRepository userRepository, final Auth0Util auth0Util,
      final SecretHashRepository secretHashRepository) {
    this.indeedHash = indeedHash;
    this.salt = salt;
    this.userRepository = userRepository;
    this.auth0Util = auth0Util;
    this.secretHashRepository = secretHashRepository;
  }

  private static String encrypt(final String value, final BytesEncryptor encryptor) {
    return Base64.getEncoder().encodeToString(encryptor.encrypt(value.getBytes(charset)));
  }

  public String encrypt(final long userId, final String value) {
    return encrypt(value, getEncryptor(userId));
  }

  private String decrypt(final long userId, final String value) {
    final User user = getUserById(userId);
    return decrypt(user, value);
  }

  private String decrypt(final User user, final String value) {
    return new String(
        getEncryptor(user).decrypt(Base64.getDecoder().decode(value)), charset);
  }

  String decrypt(final long id, final Class entityClass, final String value) {
    try {
      if (entityClass == User.class) {
        return decrypt(id, value);
      } else if (entityClass == UserPersonalInformation.class) {
        final User user = userRepository.findByUserPersonalInformationId(id);
        return decrypt(user, value);
      }
    } catch (final Exception e) {
      // for the unencoded value
      // TODO please remove after some days
      return value;
    }
    return null;
  }

  private BytesEncryptor getEncryptor(final User user) {
    return Encryptors.stronger(String.valueOf(getHashCode(user)), salt);
  }

  private BytesEncryptor getEncryptor(final long userId) {
    final User user = this.getUserById(userId);
    return Encryptors.stronger(String.valueOf(getHashCode(user)), salt);
  }

  private int getHashCode(final User user) {
    final String companyHash = getCompanyHash(user);
    final String userHash = getUserHash(user);

    return Objects.hash(this.indeedHash, companyHash, userHash);
  }


  private User getUserById(final long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
  }

  private String getUserHash(final User user) {
    return auth0Util.getUserSecret(user.getUserId());
  }

  private String getCompanyHash(final User user) {
    return secretHashRepository.getCompanySecretByCompanyId(user.getCompany().getId());
  }

}
