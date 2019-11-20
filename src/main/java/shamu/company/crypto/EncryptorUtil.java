package shamu.company.crypto;

import java.util.function.BiConsumer;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.common.entity.BaseEntity;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;

@Component
public class EncryptorUtil {

  private static final BiConsumer<UserPersonalInformation, String>
      personalInformationBiConsumer = UserPersonalInformation::setSsn;
  private static final BiConsumer<BenefitPlanDependent, String>
      benefitPlanDependentBiConsumer = BenefitPlanDependent::setSsn;
  private final Encryptor encryptor;

  @Autowired
  public EncryptorUtil(final Encryptor encryptor) {
    this.encryptor = encryptor;
  }

  private <T extends BaseEntity> void encrypt(
      final String userId,
      final String value,
      final T entity,
      final BiConsumer<T, String> setter) {

    if (Strings.isBlank(value)) {
      setter.accept(entity, null);
      return;
    }
    setter.accept(entity, encryptor.encrypt(userId, value));
  }

  private <T extends BaseEntity> void encrypt(
      final User user,
      final String value,
      final T entity,
      final BiConsumer<T, String> setter) {

    if (Strings.isBlank(value)) {
      setter.accept(entity, null);
      return;
    }
    setter.accept(entity, encryptor.encrypt(user, value));
  }

  public void encryptSsn(
      final String userId,
      final String value,
      final UserPersonalInformation entity) {
    encrypt(userId, value, entity, personalInformationBiConsumer);
  }

  public void encryptSsn(
      final User user,
      final String value,
      final UserPersonalInformation entity) {
    encrypt(user, value, entity, personalInformationBiConsumer);
  }

  public void encryptSsn(
      final String userId,
      final String value,
      final BenefitPlanDependent entity) {
    encrypt(userId, value, entity, benefitPlanDependentBiConsumer);
  }
}
