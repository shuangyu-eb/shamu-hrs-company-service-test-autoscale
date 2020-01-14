package shamu.company.crypto;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import shamu.company.company.entity.Company;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserService;

class EncryptorTests {

  @Mock
  private UserService userService;

  @Mock
  private Auth0Helper auth0Helper;

  @Mock
  private SecretHashRepository secretHashRepository;

  private User testUser;

  private final String indeedHash = "123qwe";

  private final String userSecret = "456asd";

  private final String companySecret = "789zxc";

  private final String userSalt = "3334363961363937306330613462653661386164316362663530626238333163";

  private final String ssn = "565467875";

  private String encryptedSsn;

  private Encryptor encryptor;

  @BeforeEach
  void init() throws NoSuchFieldException, IllegalAccessException {
    MockitoAnnotations.initMocks(this);

    encryptor = new Encryptor(indeedHash, userService, auth0Helper, secretHashRepository);
    initTestUser();
    initMockBehavior();
    initEncryptSsn();
  }

  private void initTestUser() throws NoSuchFieldException, IllegalAccessException {
    final String userId = "191E937FFA184508B714B9D50943D703";

    final String userPersonalInformationId = "07C5C22D11414819AB9A5A1C80A9B5EC";
    final String companyId = "070C17B3E49A4A3D9576795DC2929299";

    testUser = new User(userId);
    testUser.setCompany(new Company(companyId));

    final UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
    userPersonalInformation.setId(userPersonalInformationId);
    testUser.setUserPersonalInformation(userPersonalInformation);

    reflectSaltToTestUser();
  }

  private void reflectSaltToTestUser() throws NoSuchFieldException, IllegalAccessException {
    final Class clazz = testUser.getClass();
    final Field saltField = clazz.getDeclaredField("salt");
    saltField.setAccessible(true);
    saltField.set(testUser, userSalt);
  }

  private void initMockBehavior() {
    Mockito.when(auth0Helper.getUserSecret(testUser)).thenReturn(userSecret);
    Mockito.when(secretHashRepository.getCompanySecretByCompanyId(testUser.getCompany().getId()))
        .thenReturn(companySecret);
    Mockito.when(userService.findActiveUserById(testUser.getId())).thenReturn(testUser);
    Mockito.when(userService
        .findUserByUserPersonalInformationId(testUser.getUserPersonalInformation().getId()))
        .thenReturn(testUser);
  }

  private void initEncryptSsn() {
    final int hashCode = Objects.hash(indeedHash, companySecret, userSecret);
    final BytesEncryptor bytesEncryptor = Encryptors.stronger(String.valueOf(hashCode), userSalt);
    encryptedSsn = Base64.getEncoder()
        .encodeToString(bytesEncryptor.encrypt(ssn.getBytes(StandardCharsets.ISO_8859_1)));
    testUser.getUserPersonalInformation().setSsn(encryptedSsn);
  }

  @Test
  void testGetHashCode() throws Exception {
    final int result = Whitebox.invokeMethod(encryptor, "getHashCode", testUser);
    final int expacted = -1330890148;
    Assertions.assertEquals(expacted, result);

  }

  @Nested
  class testDecrypt {

    @Test
    void testDecryptByUser() throws Exception {
      final String result = Whitebox.invokeMethod(encryptor, "decrypt", testUser,
          testUser.getUserPersonalInformation().getSsn());
      Assertions.assertEquals(ssn, result);
    }

    @Test
    void testDecryptByUserId() throws Exception {
      final String result = Whitebox.invokeMethod(encryptor, "decrypt", testUser.getId(),
          testUser.getUserPersonalInformation().getSsn());
      Assertions.assertEquals(ssn, result);
    }

    @Test
    void whenClassIsUser_thenReturnRealResult() {
      final String result = encryptor
          .decrypt(testUser.getId(), User.class, testUser.getUserPersonalInformation().getSsn());

      Mockito.verify(userService, Mockito.times(1)).findActiveUserById(testUser.getId());
      Assertions.assertEquals(ssn, result);
    }

    @Test
    void whenClassIsUserPersonInformation_thenReturnRealResult() {
      final String result = encryptor
          .decrypt(testUser.getUserPersonalInformation().getId(), UserPersonalInformation.class,
              testUser.getUserPersonalInformation().getSsn());

      Mockito.verify(userService, Mockito.times(1))
          .findUserByUserPersonalInformationId(testUser.getUserPersonalInformation().getId());
      Assertions.assertEquals(ssn, result);
    }

    @Test
    void whenThrowException_thenReturnOriginalValue() {
      Mockito.when(userService
          .findActiveUserById(testUser.getId()))
          .thenThrow(new RuntimeException());

      final String result = encryptor
          .decrypt(testUser.getId(), User.class, testUser.getUserPersonalInformation().getSsn());

      Mockito.verify(userService, Mockito.times(1)).findActiveUserById(testUser.getId());
      Mockito.verify(userService, Mockito.never())
          .findUserByUserPersonalInformationId(testUser.getUserPersonalInformation().getId());
      Assertions.assertEquals(testUser.getUserPersonalInformation().getSsn(), result);
    }
  }

}
