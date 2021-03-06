package shamu.company.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.BiConsumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;

class EncryptorUtilTests {

  private static final BiConsumer<UserPersonalInformation, String> personalInformationBiConsumer =
      UserPersonalInformation::setSsn;
  private final String encryptedSsn = "qweasd";
  @Mock private Encryptor encryptor;
  @InjectMocks private EncryptorUtil encryptorUtil;
  private UserPersonalInformation userPersonalInformation;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);

    userPersonalInformation = new UserPersonalInformation();
    userPersonalInformation.setSsn("123");

    Mockito.when(encryptor.encrypt(Mockito.any(User.class), Mockito.anyString()))
        .thenReturn(encryptedSsn);
    Mockito.when(encryptor.encrypt(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(encryptedSsn);
  }

  @Test
  void whenEncryptByUser_valueIsBlank_thenSetToNull() throws Exception {
    Whitebox.invokeMethod(
        encryptorUtil,
        "encrypt",
        new User(),
        "",
        userPersonalInformation,
        personalInformationBiConsumer);

    Mockito.verify(encryptor, Mockito.never()).encrypt(Mockito.anyString(), Mockito.anyString());
    Assertions.assertNull(userPersonalInformation.getSsn());
  }

  @Test
  void whenEncryptByUser_valueNotNull_thenSetToNull() throws Exception {
    Whitebox.invokeMethod(
        encryptorUtil,
        "encrypt",
        new User(),
        "qwe",
        userPersonalInformation,
        personalInformationBiConsumer);

    Mockito.verify(encryptor, Mockito.times(1))
        .encrypt(Mockito.any(User.class), Mockito.anyString());
    assertThat(userPersonalInformation.getSsn()).isEqualTo(encryptedSsn);
  }

  @Nested
  class testEncryptSsn {

    private final String toBeEncryptedValue = "qwe";
    private String userId;
    private User user;
    private BenefitPlanDependent benefitPlanDependent;

    @BeforeEach
    void init() {
      userId = "asd";
      user = new User(userId);
      user.setUserPersonalInformation(userPersonalInformation);
      benefitPlanDependent = new BenefitPlanDependent();
      benefitPlanDependent.setSsn("zxc");
    }

    @Test
    void whenEncryptSsnByUser_thenSetToEncryptSsn() {
      encryptorUtil.encryptSsn(user, toBeEncryptedValue, userPersonalInformation);
      assertThat(userPersonalInformation.getSsn()).isEqualTo(encryptedSsn);
    }

    @Test
    void whenEncryptSsnByUserId_thenSetToEncryptSsn() {
      encryptorUtil.encryptSsn(userId, toBeEncryptedValue, userPersonalInformation);
      assertThat(userPersonalInformation.getSsn()).isEqualTo(encryptedSsn);
    }

    @Test
    void whenEncryptSsnByUserId_paramHasBenefitPlanDependent_thenSetToEncryptSsn() {
      encryptorUtil.encryptSsn(userId, toBeEncryptedValue, benefitPlanDependent);
      assertThat(benefitPlanDependent.getSsn()).isEqualTo(encryptedSsn);
    }
  }
}
