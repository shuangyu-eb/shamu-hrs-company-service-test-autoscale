package shamu.company.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.benefit.dto.BenefitDependentDto;
import shamu.company.user.entity.User;

class CryptoValueFilterTests {

  @Mock
  private Encryptor encryptor;

  @InjectMocks
  private CryptoValueFilter cryptoValueFilter;

  private BenefitDependentDto benefitDependentDto;

  private final String encryptedSsn = "asdzxc";

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);

    benefitDependentDto = new BenefitDependentDto();
    benefitDependentDto.setSsn(encryptedSsn);
    benefitDependentDto.setEmployeeId("123qwe");
  }

  @Test
  void testProcess() {
    cryptoValueFilter.process(benefitDependentDto, "ssn", encryptedSsn);
    Mockito.verify(encryptor, Mockito.times(1))
        .decrypt(benefitDependentDto.getEmployeeId(), User.class, benefitDependentDto.getSsn());
  }

}
