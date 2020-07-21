package shamu.company.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.company.entity.Department;
import shamu.company.utils.UuidUtil;

public class DepartmentServiceTests {

  @Mock private DepartmentRepository departmentRepository;
  private DepartmentService departmentService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    departmentService = new DepartmentService(departmentRepository);
  }

  @Test
  void testFindAllByCompanyId() {
    departmentService.findAll();
    Mockito.verify(departmentRepository, Mockito.times(1)).findAll();
  }

  @Test
  void testFindCountByDepartment() {
    departmentService.findCountByDepartment("1");
    Mockito.verify(departmentRepository, Mockito.times(1))
        .findCountByDepartment(Mockito.anyString());
  }

  @Test
  void testSave() {
    departmentService.save(new Department());
    Mockito.verify(departmentRepository, Mockito.times(1)).save(Mockito.any());
  }

  @Test
  void testDelete() {
    departmentService.delete("1");
    Mockito.verify(departmentRepository, Mockito.times(1)).delete(Mockito.anyString());
  }

  @Nested
  class testFindById {

    @Test
    void whenIdExists_thenShouldSuccess() {
      final Optional<Department> optional = Optional.of(new Department());
      Mockito.when(departmentRepository.findById(Mockito.anyString())).thenReturn(optional);
      assertThatCode(() -> departmentService.findById("1")).doesNotThrowAnyException();
    }

    @Test
    void whenIdNotExists_thenShouldThrowException() {
      final Optional<Department> optional = Optional.empty();
      Mockito.when(departmentRepository.findById(Mockito.anyString())).thenReturn(optional);
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> departmentService.findById("1"));
    }
  }

  @Test
  void findByNameAndCompanyId() {
    final List<Department> departments = new ArrayList<>();
    final Department department = new Department();
    department.setId(UuidUtil.getUuidString());

    departments.add(department);

    Mockito.when(departmentRepository.findByName("123")).thenReturn(departments);

    assertThat(departmentService.findByName("123")).isEqualTo(departments);
  }
}
