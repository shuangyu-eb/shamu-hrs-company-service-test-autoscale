package shamu.company.employee.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
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
    List<SelectFieldInformationDto> departmentDtos = departments.stream().map(department ->
        new SelectFieldInformationDto(department.getId(), department.getName())
    ).collect(Collectors.toList());
    return departmentDtos;
  }

  @Override
  public List<SelectFieldInformationDto> getEmploymentTypes() {
    List<EmploymentType> employmentTypes = employmentTypeRepository.findAll();
    List<SelectFieldInformationDto> allEmploymentTypes =
        employmentTypes.stream().map(
            employmentType -> new SelectFieldInformationDto(
                employmentType.getId(),
                employmentType.getName())
        ).collect(Collectors.toList());
    return allEmploymentTypes;
  }

  @Override
  public List<SelectFieldInformationDto> getOfficeLocations() {
    List<Office> offices = officeRepository.findAll();
    List<SelectFieldInformationDto> officeDtos = offices.stream().map(office -> {
      StringBuilder officeLocation = new StringBuilder();
      officeLocation.append(office.getName() + " ");
      OfficeAddress officeAddress = officeAddressRepository.findOfficeAddressByOffice(office);
      if (officeAddress != null) {
        officeLocation.append(officeAddress.getStreet1() + " ");
        officeLocation.append(officeAddress.getStreet2() + " ");
        officeLocation.append(officeAddress.getCity() + " ");
        officeLocation.append(officeAddress.getStateProvince().getName() + " ");
        officeLocation.append(officeAddress.getPostalCode());
      }
      return new SelectFieldInformationDto(office.getId(), officeLocation.toString());
    }).collect(Collectors.toList());
    return officeDtos;
  }

  @Override
  public List<SelectFieldInformationDto> getManagers() {
    List<User> managers = userRepository.findByUserRoleId(Contants.MANAGER_ROLE_ID);
    List<SelectFieldInformationDto> managerDtos = managers.stream().map(manager -> {
      UserPersonalInformation userInfo = manager.getUserPersonalInformation();
      String firstName = userInfo.getFirstName();
      String middleName = userInfo.getMiddleName();
      String lastName = userInfo.getLastName();
      String name = firstName + " " + middleName + " " + lastName;
      return new SelectFieldInformationDto(manager.getId(), name);
    }).collect(Collectors.toList());
    return managerDtos;
  }

  @Override
  public EmploymentType saveEmploymentType(String employmentTypeName) {
    EmploymentType employmentType = new EmploymentType();
    employmentType.setName(employmentTypeName);
    return employmentTypeRepository.save(employmentType);
  }

  @Override
  public Department saveDepartment(String departmentName) {
    Department department = new Department();
    department.setName(departmentName);
    return departmentRepository.save(department);
  }

  @Override
  public Office saveOfficeLocation(OfficePojo officePojo) {
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
    Office office = new Office();
    office.setName(officePojo.getOfficeName());
    office.setOfficeAddress(officeAddressReturned);
    return officeRepository.save(office);
  }
}
