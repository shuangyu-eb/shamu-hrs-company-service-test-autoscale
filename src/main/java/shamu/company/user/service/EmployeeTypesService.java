package shamu.company.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.user.entity.EmployeeType;
import shamu.company.user.repository.EmployeeTypesRepository;

import java.util.List;

@Service
@Transactional
public class EmployeeTypesService {

  private final EmployeeTypesRepository employeeTypesRepository;

  @Autowired
  public EmployeeTypesService(final EmployeeTypesRepository employeeTypesRepository) {
    this.employeeTypesRepository = employeeTypesRepository;
  }

  public List<EmployeeType> findAll() {
    return employeeTypesRepository.findAll();
  }

  public EmployeeType findById(final String id) {
    return employeeTypesRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("EmployeeType with id %s not found!", id), id, "employeeType"));
  }
}
