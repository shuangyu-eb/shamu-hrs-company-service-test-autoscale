package shamu.company.employee.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.EmploymentTypeRepository;
import shamu.company.employee.entity.EmploymentType;

@Service
@Transactional
public class EmploymentTypeService {

  private final EmploymentTypeRepository employmentTypeRepository;

  @Autowired
  public EmploymentTypeService(final EmploymentTypeRepository employmentTypeRepository) {
    this.employmentTypeRepository = employmentTypeRepository;
  }

  public EmploymentType findById(final String id) {
    return employmentTypeRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Employment type with id %s not found.", id)));
  }

  public List<EmploymentType> findAllByCompanyId(String companyId) {
    return employmentTypeRepository.findAllByCompanyId(companyId);
  }

  public Integer findCountByType(String typeId) {
    return employmentTypeRepository.findCountByType(typeId);
  }

  public EmploymentType save(EmploymentType employmentType) {
    return employmentTypeRepository.save(employmentType);
  }

  public void delete(String id) {
    employmentTypeRepository.delete(id);
  }
}
