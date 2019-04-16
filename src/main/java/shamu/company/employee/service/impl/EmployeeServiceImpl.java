package shamu.company.employee.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.entity.StateProvince;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.employee.dto.GeneralObjectDto;
import shamu.company.employee.entity.CompensationFrequency;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.pojo.OfficePojo;
import shamu.company.employee.repository.CompensationFrequencyRepository;
import shamu.company.employee.repository.DepartmentRepository;
import shamu.company.employee.repository.EmploymentTypeRepository;
import shamu.company.employee.repository.GenderRepository;
import shamu.company.employee.repository.MartialStatusRepository;
import shamu.company.employee.repository.OfficeAddressRepository;
import shamu.company.employee.repository.OfficeRepository;
import shamu.company.employee.repository.StateProvinceRepository;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.JobUserRepository;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
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

  @Autowired
  private CompensationFrequencyRepository compensationFrequencyRepository;

  @Autowired
  private GenderRepository genderRepository;

  @Autowired
  private MartialStatusRepository martialStatusRepository;

  private GeneralObjectDto getGeneralDtos(List list, String dtoId, Class clazz) {
    List<GeneralObjectDto> dtos = new ArrayList<>();
    Method idMethod = null;
    Method nameMethod = null;
    try {
      idMethod = clazz.getMethod("getId");
      nameMethod = clazz.getMethod("getName");
      Method finalIdMethod = idMethod;
      finalIdMethod.setAccessible(true);
      Method finalNameMethod = nameMethod;
      finalNameMethod.setAccessible(true);
      list.forEach(element -> {
        GeneralObjectDto dto = new GeneralObjectDto();
        String id = null;
        String name = null;
        try {
          id = String.valueOf(finalIdMethod.invoke(element));
          name = (String) finalNameMethod.invoke(element);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          e.printStackTrace();
        }
        dto.setId(id);
        dto.setName(name);
        dtos.add(dto);
      });
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      return null;
    }
    GeneralObjectDto listDto = new GeneralObjectDto();
    listDto.setId(dtoId);
    listDto.setName(dtos);
    return listDto;
  }

  @Override
  public List<GeneralObjectDto> getJobInformation() {
    List<GeneralObjectDto> results = new ArrayList<>();

    List<Department> departmentList = departmentRepository.findAll();
    results.add(getGeneralDtos(departmentList, "department", Department.class));

    List<User> managerList = userRepository.findAllManagers();
    List<GeneralObjectDto> allManagers = new ArrayList<>();
    for (User manager : managerList) {
      GeneralObjectDto managerDto = new GeneralObjectDto();
      String id = String.valueOf(manager.getId());
      UserPersonalInformation userInfo = manager.getUserPersonalInformation();
      String firstName = userInfo.getFirstName();
      String middleName = userInfo.getMiddleName();
      String lastName = userInfo.getLastName();
      String name = firstName + middleName + lastName;
      managerDto.setId(id);
      managerDto.setName(name);
      allManagers.add(managerDto);
    }
    GeneralObjectDto allManagersDtos = new GeneralObjectDto();
    allManagersDtos.setId("reportsTo");
    allManagersDtos.setName(allManagers);
    results.add(allManagersDtos);

    List<EmploymentType> employmentTypes = employmentTypeRepository.findAll();
    results.add(getGeneralDtos(employmentTypes, "employmentType", EmploymentType.class));

    List<StateProvince> stateProvinces = stateProvinceRepository.findAll();
    results.add(getGeneralDtos(stateProvinces, "state", StateProvince.class));

    List<Office> offices = officeRepository.findAll();
    List<GeneralObjectDto> officeDtos = new ArrayList<>();
    for(Office office : offices) {
      GeneralObjectDto officeDto = new GeneralObjectDto();
      officeDto.setId(String.valueOf(office.getId()));
      StringBuilder sb = new StringBuilder();
      sb.append(office.getName() + " ");
      OfficeAddress officeAddress = officeAddressRepository.findOfficeAddressByOffice(office);
      if(officeAddress != null){
        sb.append(officeAddress.getStreet1() + " ");
        sb.append(officeAddress.getStreet2() + " ");
        sb.append(officeAddress.getCity() + " ");
        sb.append(officeAddress.getStateProvince().getName() + " ");
        sb.append(officeAddress.getPostalCode());
        officeDto.setName(sb.toString());
        officeDtos.add(officeDto);
      }
    }
    GeneralObjectDto allofficeDtos = new GeneralObjectDto();
    allofficeDtos.setId("office");
    allofficeDtos.setName(officeDtos);
    results.add(allofficeDtos);

    List<Gender> genders = genderRepository.findAll();
    results.add(getGeneralDtos(genders, "gender", Gender.class));

    List<MaritalStatus> martialStatuses = martialStatusRepository.findAll();
    results.add(getGeneralDtos(martialStatuses, "maritalStatus", MaritalStatus.class));

    List<CompensationFrequency> compensationFrequencies = compensationFrequencyRepository.findAll();
    results.add(
        getGeneralDtos(compensationFrequencies, "compensationUnit", CompensationFrequency.class));

    return results;
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
