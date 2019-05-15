package shamu.company.job.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.job.service.DepartmentService;

@Service
public class DepartmentServiceImpl implements DepartmentService {

  private final DepartmentRepository departmentRepository;

  @Autowired
  public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
    this.departmentRepository = departmentRepository;
  }

  @Override
  public List<Department> getDepartmentsByCompany(Company company) {
    return departmentRepository.findAllByCompany(company);
  }
}
