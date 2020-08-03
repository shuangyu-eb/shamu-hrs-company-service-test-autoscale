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
    List<TimeSheetPeriodPojo> timeSheetPeriodPojos;
    List<TimePeriod> timePeriodList;
    TimePeriod timePeriod;

    @BeforeEach
    void init() {
      userId = "test_user_id";
      timeSheetPeriodPojos = new ArrayList<>();
      companyId = "test_company_id";
      timePeriod = new TimePeriod();
    }

    @Test
    void whenUserIdValid_shouldSucceed() {
      Mockito.when(timePeriodRepository.listTimeSheetPeriodsByUser(userId))
          .thenReturn(timeSheetPeriodPojos);
      assertThatCode(() -> timePeriodService.listByUser(userId)).doesNotThrowAnyException();
    }

    @Test
    void whenCompanyIdValid_shouldSucceed() {
      Mockito.when(timePeriodRepository.findByCompanyId(companyId)).thenReturn(timePeriodList);
      assertThatCode(() -> timePeriodService.listByCompany(companyId)).doesNotThrowAnyException();
    }

    @Test
    void whenUserIdValid_findLatestPeriod_shouldSucceed() {
      Mockito.when(timePeriodRepository.findLatestPeriodByUser(userId))
          .thenReturn(new TimePeriod());
      assertThatCode(() -> timePeriodService.findUserLatestPeriod(userId))
          .doesNotThrowAnyException();
    }

    @Test
    void whenCompanyIdValid_findCurrentPeriod_shouldSucceed() {
      Mockito.when(timePeriodRepository.findCompanyNewestPeriod(companyId)).thenReturn(timePeriod);
      assertThatCode(() -> timePeriodService.findCompanyCurrentPeriod(companyId))
          .doesNotThrowAnyException();
    }
  }
}
