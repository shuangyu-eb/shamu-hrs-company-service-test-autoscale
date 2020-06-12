package shamu.company.attendance;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.repository.PayPeriodFrequencyRepository;
import shamu.company.attendance.service.PayPeriodFrequencyService;

public class PayPeriodFrequencyServiceTest {

    @InjectMocks
    PayPeriodFrequencyService payPeriodFrequencyService;

    @Mock private PayPeriodFrequencyRepository payPeriodFrequencyRepository;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findAllShouldSucceed() {
        assertThatCode(
                ()-> { payPeriodFrequencyService.findAll(); }
        ).doesNotThrowAnyException();
    }
}
