package shamu.company.employee.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.repository.StateProvinceRepository;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.employee.Contants;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.pojo.OfficePojo;
import shamu.company.employee.repository.DepartmentRepository;
import shamu.company.employee.repository.EmploymentTypeRepository;
import shamu.company.employee.repository.OfficeAddressRepository;
import shamu.company.employee.repository.OfficeRepository;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.JobUserRepository;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.repository.UserRepository;

@Service
public class EmployeeServiceImpl implements EmployeeService {

  @Autowired
  UserRepository userRepository;

  @Autowired
  JobUserRepository jobUserRepository;

  @Autowired
  UserAddressRepository userAddressRepository;

  @Autowired
  DepartmentRepository departmentRepository;

  @Autowired
  private EmploymentTypeRepository employmentTypeRepository;

  @Autowired
  private OfficeRepository officeRepository;

  @Autowired
  private OfficeAddressRepository officeAddressRepository;

  @Autowired
  private StateProvinceRepository stateProvinceRepository;

  @Override
  public List<SelectFieldInformationDto> getDepartments() {
    List<Department> departments = departmentRepository.findAll();
    List<SelectFieldInformationDto> departmentDtos = new ArrayList<>();
    departments.forEach(department -> {
      SelectFieldInformationDto departmentDto = new SelectFieldInformationDto();
      departmentDto.setId(department.getId());
      departmentDto.setName(department.getName());
      departmentDtos.add(departmentDto);
    });
    return departmentDtos;
  }

  @Override
  public List<SelectFieldInformationDto> getEmploymentTypes() {
    List<EmploymentType> employmentTypes = employmentTypeRepository.findAll();
    List<SelectFieldInformationDto> allEmploymentTypes = new ArrayList<>();
    employmentTypes.forEach(employmentType -> {
      SelectFieldInformationDto employmentTypeDto = new SelectFieldInformationDto();
      employmentTypeDto.setId(employmentType.getId());
      employmentTypeDto.setName(employmentType.getName());
      allEmploymentTypes.add(employmentTypeDto);
    });
    return allEmploymentTypes;
  }

  @Override
  public List<SelectFieldInformationDto> getOfficeLocations() {
    List<Office> offices = officeRepository.findAll();
    List<SelectFieldInformationDto> officeDtos = new ArrayList<>();
    offices.forEach(office -> {
      SelectFieldInformationDto officeDto = new SelectFieldInformationDto();
      officeDto.setId(office.getId());
      StringBuilder sb = new StringBuilder();
      sb.append(office.getName() + " ");
      OfficeAddress officeAddress = officeAddressRepository.findOfficeAddressByOffice(office);
      if (officeAddress != null) {
        sb.append(officeAddress.getStreet1() + " ");
        sb.append(officeAddress.getStreet2() + " ");
        sb.append(officeAddress.getCity() + " ");
        sb.append(officeAddress.getStateProvince().getName() + " ");
        sb.append(officeAddress.getPostalCode());
        officeDto.setName(sb.toString());
        officeDtos.add(officeDto);
      }
    });
    return officeDtos;
  }

  @Override
  public List<SelectFieldInformationDto> getManagers() {
    List<User> managers = userRepository.findByUserRoleId(Contants.MANAGER_ROLE_ID);
    List<SelectFieldInformationDto> managerDtos = new ArrayList<>();
    managers.forEach(manager -> {
      SelectFieldInformationDto managerDto = new SelectFieldInformationDto();
      UserPersonalInformation userInfo = manager.getUserPersonalInformation();
      String firstName = userInfo.getFirstName();
      String middleName = userInfo.getMiddleName();
      String lastName = userInfo.getLastName();
      String name = firstName + middleName + lastName;
      managerDto.setId(manager.getId());
      managerDto.setName(name);
      managerDtos.add(managerDto);
    });
    return managerDtos;
  }

  @Override
  public Long saveEmploymentType(String employmentTypeName) {
    EmploymentType employmentType = new EmploymentType();
    employmentType.setName(employmentTypeName);
    EmploymentType employmentTypeReturned = employmentTypeRepository.save(employmentType);
    return employmentTypeReturned.getId();

  }

  @Override
  public Long saveDepartment(String departmentName) {
    Department department = new Department();
    department.setName(departmentName);
    Department departmentReturned = departmentRepository.save(department);
    return departmentReturned.getId();


  }

  @Override
  public Long saveOfficeLocation(OfficePojo officePojo) {
    final Office office = new Office();
    OfficeAddress officeAddress = new OfficeAddress();
    officeAddress.setCity(officePojo.getCity());
    if (!"".equals(officePojo.getState()) && null != officePojo.getState()) {
      StateProvince state = stateProvinceRepository
          .findById(Long.parseLong(officePojo.getState())).get();
      officeAddress.setStateProvince(state);
    }
    officeAddress.setPostalCode(officePojo.getZip());
    officeAddress.setStreet1(officePojo.getStreet1());
    officeAddress.setStreet2(officePojo.getStreet2());
    OfficeAddress officeAddressReturned = officeAddressRepository.save(officeAddress);
    office.setName(officePojo.getOfficeName());
    office.setOfficeAddress(officeAddressReturned);
    Office officeReturned = officeRepository.save(office);
    return officeReturned.getId();
  }
}
