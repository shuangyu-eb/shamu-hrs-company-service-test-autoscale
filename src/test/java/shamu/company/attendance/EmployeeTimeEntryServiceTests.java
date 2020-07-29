package shamu.company.attendance;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.entity.EmployeeTimeEntry;
import shamu.company.attendance.repository.EmployeeTimeEntryRepository;
import shamu.company.attendance.service.EmployeeTimeEntryService;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;

public class EmployeeTimeEntryServiceTests {
  @InjectMocks EmployeeTimeEntryService employeeTimeEntryService;

  @Mock private EmployeeTimeEntryRepository employeeTimeEntryRepository;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class findTimeSheetById {
    EmployeeTimeEntry employeeTimeEntry;

    @BeforeEach
    void init() {
      employeeTimeEntry = new EmployeeTimeEntry();
    }

    @Test
    void whenIdExists_thenShouldSuccess() {
      Mockito.when(employeeTimeEntryRepository.findById(Mockito.anyString()))
          .thenReturn(Optional.ofNullable(employeeTimeEntry));
      assertThatCode(() -> employeeTimeEntryService.findById("1")).doesNotThrowAnyException();
    }

    @Test
    void whenIdNotExists_thenShouldThrow() {
      Mockito.when(employeeTimeEntryRepository.findById(Mockito.anyString()))
          .thenReturn(Optional.empty());
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> employeeTimeEntryService.findById("1"));
    }
  }

  @Test
  void deleteMyHourEntry() {
    assertThatCode(() -> employeeTimeEntryService.deleteMyHourEntry("1"))
        .doesNotThrowAnyException();
  }
}
