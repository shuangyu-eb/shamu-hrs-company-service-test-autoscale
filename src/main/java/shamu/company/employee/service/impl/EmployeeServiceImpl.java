package shamu.company.employee.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.common.repository.EmploymentTypeRepository;
import shamu.company.common.repository.OfficeAddressRepository;
import shamu.company.common.repository.OfficeRepository;
import shamu.company.common.repository.StateProvinceRepository;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.employee.Contants;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.pojo.OfficePojo;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.repository.JobUserRepository;
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
      List<String> officeLocationDetails = new ArrayList<>();
      String officeName = office.getName();
      if (null != officeName && !"".equals(officeName)) {
        officeLocationDetails.add(officeName);
      }
      OfficeAddress officeAddress = officeAddressRepository.findOfficeAddressByOffice(office);
      if (null != officeAddress) {
        String street1 = officeAddress.getStreet1();
        if (null != street1 && !"".equals(street1)) {
          officeLocationDetails.add(street1);
        }
        String street2 = officeAddress.getStreet2();
        if (null != street2 && !"".equals(street2)) {
          officeLocationDetails.add(street2);
        }
        String city = officeAddress.getCity();
        if (null != city && !"".equals(city)) {
          officeLocationDetails.add(city);
        }
        String state = officeAddress.getStateProvince().getName();
        if (null != state && !"".equals(state)) {
          officeLocationDetails.add(state);
        }
        String postalCode = officeAddress.getPostalCode();
        if (null != postalCode && !"".equals(postalCode)) {
          officeLocationDetails.add(postalCode);
        }
      }
      String officeLocation = String.join(" ", officeLocationDetails.toArray(new String[0]));
      return new SelectFieldInformationDto(office.getId(), officeLocation);
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
      List<String> nameDetails = new ArrayList<>();
      if (null != firstName && !"".equals(firstName)) {
        nameDetails.add(firstName);
      }
      if (null != middleName && !"".equals(middleName)) {
        nameDetails.add(middleName);
      }
      if (null != lastName && !"".equals(lastName)) {
        nameDetails.add(lastName);
      }
      String name = String.join(" ", nameDetails.toArray(new String[0]));
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
    OfficeAddress officeAddress = officePojo.getOfficeAddress();
    OfficeAddress officeAddressReturned = officeAddressRepository.save(officeAddress);
    Office office = officePojo.getOffice();
    office.setOfficeAddress(officeAddressReturned);
    return officeRepository.save(office);
  }
}
