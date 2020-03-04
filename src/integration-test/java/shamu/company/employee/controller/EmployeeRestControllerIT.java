package shamu.company.employee.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import shamu.company.BaseIntegrationTest;
import shamu.company.authorization.Permission.Name;
import shamu.company.common.service.OfficeService;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Office;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.NewEmployeeJobInformationDto;
import shamu.company.employee.dto.WelcomeEmailDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmploymentTypeService;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.Job;
import shamu.company.job.service.JobService;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.service.CompensationFrequencyService;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

class EmployeeRestControllerIT extends BaseIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private UserService userService;

  @Autowired
  private JobService jobService;

  @Autowired
  private EmploymentTypeService employmentTypeService;

  @Autowired
  private OfficeService officeService;

  @Autowired
  private UserRoleService userRoleService;

  @Autowired
  private CompensationFrequencyService compensationFrequencyService;

  @Nested
  class AddEmployee {

    private User manager;
    private CompensationFrequency compensationFrequency;
    private Job job;
    private EmploymentType employmentType;
    private Office office;

    @BeforeEach
    void init() {
      final User managerUser = new User();
      final Company company = new Company(getAuthUser().getCompanyId());
      managerUser.setCompany(company);
      managerUser.setUserContactInformation(new UserContactInformation());
      managerUser.setId(UuidUtil.getUuidString());
      managerUser.setUserRole(userRoleService.getEmployee());
      manager = userService.save(managerUser);

      final List<CompensationFrequency> allFrequencies = compensationFrequencyService.findAll();
      compensationFrequency = allFrequencies.get(0);

      final Job newJob = new Job();
      newJob.setTitle(RandomStringUtils.randomAlphabetic(4));
      job = jobService.save(newJob);

      final EmploymentType newEmploymentType = new EmploymentType();
      newEmploymentType.setName(RandomStringUtils.randomAlphabetic(4));
      newEmploymentType.setCompany(company);
      employmentType = employmentTypeService.save(newEmploymentType);

      final Office newOffice = new Office();
      newOffice.setName(RandomStringUtils.randomAlphabetic(4));
      newOffice.setCompany(company);
      office = officeService.save(newOffice);
    }

    @Test
    void addEmployee() throws Exception {
      setPermission(Name.CREATE_USER.name());
      given(auth0Helper.getUserId(Mockito.any())).willReturn(UuidUtil.getUuidString());

      final UserAddressDto userAddressDto = new UserAddressDto();
      userAddressDto.setCity("A city");
      userAddressDto.setPostalCode("222000");
      userAddressDto.setStreet1("Street 1");
      userAddressDto.setStreet2("Street 2");

      final WelcomeEmailDto welcomeEmailDto = new WelcomeEmailDto();
      welcomeEmailDto.setPersonalInformation("Knock knock");
      welcomeEmailDto.setSendDate(Timestamp.valueOf(DateUtil.getLocalUtcTime()));
      welcomeEmailDto.setSendTo("touser@example.com");

      final NewEmployeeJobInformationDto jobInformationDto = new NewEmployeeJobInformationDto();
      jobInformationDto.setReportsTo(manager.getId());
      jobInformationDto.setCompensation(4.0);
      jobInformationDto.setCompensationFrequencyId(compensationFrequency.getId());
      jobInformationDto.setJobId(job.getId());
      jobInformationDto.setEmploymentTypeId(employmentType.getId());
      jobInformationDto.setHireDate(Timestamp.valueOf(DateUtil.getLocalUtcTime()));
      jobInformationDto.setOfficeId(office.getOfficeId());

      final UserEmergencyContactDto emergencyContactDto = new UserEmergencyContactDto();
      emergencyContactDto.setEmail("em@example.com");
      emergencyContactDto.setFirstName("Emergency");
      emergencyContactDto.setLastName("Jo");
      emergencyContactDto.setRelationship("XXX");
      final List<UserEmergencyContactDto> emergencyContactDtos = Collections.singletonList(emergencyContactDto);

      final EmployeeDto employeeDto = EmployeeDto.builder()
          .emailWork("aemailaddress@example.com")
          .userAddress(userAddressDto)
          .welcomeEmail(welcomeEmailDto)
          .jobInformation(jobInformationDto)
          .userEmergencyContactDto(emergencyContactDtos)
          .build();

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

      final MvcResult result = mvc.perform(post("/company/employees")
          .contentType(MediaType.APPLICATION_JSON)
          .headers(httpHeaders)
          .content(JsonUtil.formatToString(employeeDto))).andReturn();

      assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }
  }
}
