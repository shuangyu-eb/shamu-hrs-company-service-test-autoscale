package shamu.company.employee;

import static org.assertj.core.api.Assertions.assertThat;
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
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.repository.EmploymentTypeRepository;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmploymentTypeService;

class EmploymentTypeServiceTests {
  @InjectMocks EmploymentTypeService employmentTypeService;

  @Mock private EmploymentTypeRepository employmentTypeRepository;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void testFindAllByCompanyId() {
    assertThatCode(() -> employmentTypeService.findAllByCompanyId("1")).doesNotThrowAnyException();
  }

  @Test
  void testFindCountByType() {
    assertThatCode(() -> employmentTypeService.findCountByType("1")).doesNotThrowAnyException();
  }

  @Test
  void testSave() {
    assertThatCode(() -> employmentTypeService.save(new EmploymentType()))
        .doesNotThrowAnyException();
  }

  @Test
  void testDelete() {
    assertThatCode(() -> employmentTypeService.delete("1")).doesNotThrowAnyException();
  }

  @Nested
  class findById {
    String id = "a";
    EmploymentType employmentType;

    @BeforeEach
    void init() {
      employmentType = new EmploymentType();
    }

    @Test
    void whenEmploymentTypeIsNotNull_thenShouldSuccess() {
      Mockito.when(employmentTypeRepository.findById(id))
          .thenReturn(java.util.Optional.ofNullable(employmentType));
      final EmploymentType employmentType = employmentTypeService.findById(id);
      assertThat(employmentType).isNotNull();
    }

    @Test
    void whenEmploymentTypeIsNull_thenShouldThrow() {
      Mockito.when(employmentTypeRepository.findById(id)).thenReturn(Optional.empty());
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> employmentTypeService.findById(id));
    }
  }
}
