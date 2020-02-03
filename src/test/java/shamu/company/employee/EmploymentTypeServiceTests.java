package shamu.company.employee;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.EmploymentTypeRepository;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmploymentTypeService;

import java.util.Optional;

class EmploymentTypeServiceTests {
  @InjectMocks EmploymentTypeService employmentTypeService;

  @Mock private EmploymentTypeRepository employmentTypeRepository;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
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
          ResourceNotFoundException.class, () -> employmentTypeService.findById(id));
    }
  }
}
