package shamu.company.common.service;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.OldResourceNotFoundException;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.company.entity.Department;

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
    departmentService.findAllByCompanyId("1");
    Mockito.verify(departmentRepository, Mockito.times(1)).findAllByCompanyId(Mockito.anyString());
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
      Assertions.assertDoesNotThrow(() -> departmentService.findById("1"));
    }

    @Test
    void whenIdNotExists_thenShouldThrowException() {
      final Optional<Department> optional = Optional.empty();
      Mockito.when(departmentRepository.findById(Mockito.anyString())).thenReturn(optional);
      Assertions.assertThrows(
          OldResourceNotFoundException.class, () -> departmentService.findById("1"));
    }
  }
}
