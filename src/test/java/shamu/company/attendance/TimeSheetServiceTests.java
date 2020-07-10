package shamu.company.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.repository.TimeSheetRepository;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TimeSheetServiceTests {

  @InjectMocks TimeSheetService timeSheetService;

  @Mock private TimeSheetRepository timeSheetRepository;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class findTimeSheetById {
    TimeSheet timeSheet;

    @BeforeEach
    void init() {
      timeSheet = new TimeSheet();
    }

    @Test
    void whenIdExists_thenShouldSuccess() {
      Mockito.when(timeSheetRepository.findById(Mockito.anyString()))
          .thenReturn(Optional.ofNullable(timeSheet));
      assertThatCode(() -> timeSheetService.findTimeSheetById("1")).doesNotThrowAnyException();
    }

    @Test
    void whenIdNotExists_thenShouldThrow() {
      Mockito.when(timeSheetRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> timeSheetService.findTimeSheetById("1"));
    }
  }

  @Nested
  class saveAll {
    List<TimeSheet> timeSheetList;

    @BeforeEach
    void init() {
      timeSheetList = new ArrayList<>();
    }

    @Test
    void whenListValid_shouldSucceed() {
      Mockito.when(timeSheetRepository.saveAll(timeSheetList)).thenReturn(Mockito.any());
      assertThatCode(() -> timeSheetService.saveAll(timeSheetList)).doesNotThrowAnyException();
    }
  }
}
