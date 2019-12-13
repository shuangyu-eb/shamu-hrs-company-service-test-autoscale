package shamu.company.common.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.company.entity.Department;


@Service
@Transactional
public class DepartmentService {

  private final DepartmentRepository departmentRepository;

  @Autowired
  public DepartmentService(final DepartmentRepository departmentRepository) {
    this.departmentRepository = departmentRepository;
  }

  public List<Department> findAllByCompanyId(String companyId) {
    return departmentRepository.findAllByCompanyId(companyId);
  }

  public Integer findCountByDepartment(String departmentId) {
    return departmentRepository.findCountByDepartment(departmentId);
  }

  public Department findById(String id) {
    return departmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("No Department with id: " + id));
  }

  public Department save(Department department) {
    return departmentRepository.save(department);
  }

  public void delete(String id) {
    departmentRepository.delete(id);
  }
}
