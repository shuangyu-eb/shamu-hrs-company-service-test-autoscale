package shamu.company.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheetPeriodPojo;
import shamu.company.attendance.repository.TimePeriodRepository;
import shamu.company.attendance.service.TimePeriodService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

public class TimePeriodServiceTests {
  @InjectMocks TimePeriodService timePeriodService;

  @Mock private TimePeriodRepository timePeriodRepository;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class get {
    String userId;
    String companyId;
    List<TimeSheetPeriodPojo> timePeriodList;
    TimePeriod timePeriod;

    @BeforeEach
    void init() {
      userId = "test_user_id";
      timePeriodList = new ArrayList<>();
      companyId = "test_company_id";
      timePeriod = new TimePeriod();
    }

    @Test
    void whenUserIdValid_shouldSucceed() {
      Mockito.when(timePeriodRepository.listTimeSheetPeriodsByUser(userId))
          .thenReturn(timePeriodList);
      assertThatCode(() -> timePeriodService.listByUser(userId)).doesNotThrowAnyException();
    }

    @Test
    void whenCompanyIdValid_shouldSucceed() {
      Mockito.when(timePeriodRepository.findCompanyNewestPeriod(companyId)).thenReturn(timePeriod);
      assertThatCode(() -> timePeriodService.findCompanyCurrentPeriod(companyId))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  class save {
    TimePeriod timePeriod;

    @BeforeEach
    void init() {
      timePeriod = new TimePeriod(new Date(), new Date());
    }

    @Test
    void whenTimePeriodValid_shouldSucceed() {
      Mockito.when(timePeriodRepository.save(timePeriod)).thenReturn(timePeriod);
      assertThatCode(() -> timePeriodService.createIfNotExist(timePeriod))
          .doesNotThrowAnyException();
    }
  }
}
