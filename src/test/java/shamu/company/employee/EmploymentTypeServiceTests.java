package shamu.company.employee;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.OldResourceNotFoundException;
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
    Assertions.assertDoesNotThrow(() -> employmentTypeService.findAllByCompanyId("1"));
  }

  @Test
  void testFindCountByType() {
    Assertions.assertDoesNotThrow(() -> employmentTypeService.findCountByType("1"));
  }

  @Test
  void testSave() {
    Assertions.assertDoesNotThrow(() -> employmentTypeService.save(new EmploymentType()));
  }

  @Test
  void testDelete() {
    Assertions.assertDoesNotThrow(() -> employmentTypeService.delete("1"));
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
      Assertions.assertNotNull(employmentType);
    }

    @Test
    void whenEmploymentTypeIsNull_thenShouldThrow() {
      Mockito.when(employmentTypeRepository.findById(id)).thenReturn(Optional.empty());
      Assertions.assertThrows(
          OldResourceNotFoundException.class, () -> employmentTypeService.findById(id));
    }
  }
}
