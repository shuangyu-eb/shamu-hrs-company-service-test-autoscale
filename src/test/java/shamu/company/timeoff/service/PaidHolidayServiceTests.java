package shamu.company.timeoff.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.company.entity.Company;
import shamu.company.helpers.FederalHolidayHelper;
import shamu.company.job.dto.JobUserDto;
import shamu.company.server.dto.AuthUser;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.dto.PaidHolidayEmployeeDto;
import shamu.company.timeoff.entity.CompanyPaidHoliday;
import shamu.company.timeoff.entity.PaidHoliday;
import shamu.company.timeoff.entity.PaidHolidayUser;
import shamu.company.timeoff.entity.mapper.CompanyPaidHolidayMapper;
import shamu.company.timeoff.entity.mapper.PaidHolidayMapper;
import shamu.company.timeoff.repository.CompanyPaidHolidayRepository;
import shamu.company.timeoff.repository.PaidHolidayRepository;
import shamu.company.timeoff.repository.PaidHolidayUserRepository;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class PaidHolidayServiceTests {

  @Mock
  private PaidHolidayRepository paidHolidayRepository;

  @Mock
  private CompanyPaidHolidayRepository companyPaidHolidayRepository;

  @Mock
  private UserService userService;

  @Mock
  private PaidHolidayUserRepository paidHolidayUserRepository;

  @Mock
  private CompanyPaidHolidayMapper companyPaidHolidayMapper;

  @Mock
  private PaidHolidayMapper paidHolidayMapper;

  @Mock
  private FederalHolidayHelper federalHolidayHelper;

  private static PaidHolidayService paidHolidayService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    paidHolidayService = new PaidHolidayService(
        paidHolidayRepository,
        companyPaidHolidayRepository,
        userService,
        paidHolidayUserRepository,
        companyPaidHolidayMapper,
        paidHolidayMapper,
        federalHolidayHelper
    );
  }

  @Test
  void initDefaultPaidHolidays() {
    final List<PaidHoliday> paidHolidayList = new ArrayList<>();
    final PaidHoliday paidHoliday = new PaidHoliday();
    paidHolidayList.add(paidHoliday);

    final List<CompanyPaidHoliday> companyPaidHolidayList = new ArrayList<>();

    Mockito.when(paidHolidayRepository.findDefaultPaidHolidays()).thenReturn(paidHolidayList);
    Mockito.when(companyPaidHolidayRepository.saveAll(Mockito.any())).thenReturn(companyPaidHolidayList);

    Assertions.assertDoesNotThrow(() ->
        paidHolidayService.initDefaultPaidHolidays(new Company("1")));

  }

  @Test
  void getPaidHolidays() {
    final AuthUser authUser = new AuthUser();
    final List<CompanyPaidHoliday> companyPaidHolidays = new ArrayList<>();
    final CompanyPaidHoliday companyPaidHoliday = new CompanyPaidHoliday();
    companyPaidHolidays.add(companyPaidHoliday);

    final PaidHolidayDto paidHolidayDto = new PaidHolidayDto();
    paidHolidayDto.setName("007");
    paidHolidayDto.setFederal(true);

    Mockito.when(companyPaidHolidayRepository.findAllByCompanyId(Mockito.any())).thenReturn(companyPaidHolidays);
    Mockito.when(companyPaidHolidayMapper.convertToPaidHolidayDto(Mockito.any(), Mockito.any())).thenReturn(paidHolidayDto);

    Assertions.assertDoesNotThrow(() ->
        paidHolidayService.getPaidHolidays(authUser));
  }

  @Test
  void getUserPaidHolidays() {
    final AuthUser authUser = new AuthUser();
    final List<CompanyPaidHoliday> companyPaidHolidays = new ArrayList<>();
    final CompanyPaidHoliday companyPaidHoliday = new CompanyPaidHoliday();
    companyPaidHolidays.add(companyPaidHoliday);

    final PaidHolidayDto paidHolidayDto = new PaidHolidayDto();
    paidHolidayDto.setName("007");
    paidHolidayDto.setFederal(true);

    Mockito.when(companyPaidHolidayRepository.findAllByCompanyIdAndUserId(Mockito.any(), Mockito.any())).thenReturn(companyPaidHolidays);
    Mockito.when(companyPaidHolidayMapper.convertToPaidHolidayDto(Mockito.any(), Mockito.any())).thenReturn(paidHolidayDto);

    Assertions.assertDoesNotThrow(() ->
        paidHolidayService.getUserPaidHolidays(authUser, "1"));
  }

  @Test
  void getPaidHolidaysByYear() {
    final AuthUser authUser = new AuthUser();
    final List<CompanyPaidHoliday> companyPaidHolidays = new ArrayList<>();
    final CompanyPaidHoliday companyPaidHoliday = new CompanyPaidHoliday();
    companyPaidHolidays.add(companyPaidHoliday);

    final PaidHolidayDto paidHolidayDto = new PaidHolidayDto();
    paidHolidayDto.setName("007");
    paidHolidayDto.setFederal(true);
    paidHolidayDto.setDate(Timestamp.valueOf(LocalDateTime.now()));

    Mockito.when(companyPaidHolidayRepository.findAllByCompanyIdAndUserId(Mockito.any(), Mockito.any())).thenReturn(companyPaidHolidays);
    Mockito.when(companyPaidHolidayRepository.findAllByCompanyId(Mockito.any())).thenReturn(companyPaidHolidays);
    Mockito.when(companyPaidHolidayMapper.convertToPaidHolidayDto(Mockito.any(), Mockito.any())).thenReturn(paidHolidayDto);

    Assertions.assertDoesNotThrow(() ->
        paidHolidayService.getPaidHolidaysByYear(authUser, "2020"));
  }

  @Test
  void updateHolidaySelects() {
    final List<PaidHolidayDto> paidHolidayDtos = new ArrayList<>();
    final PaidHolidayDto paidHolidayDto = new PaidHolidayDto();
    paidHolidayDto.setId("1");
    paidHolidayDto.setIsSelected(true);
    paidHolidayDtos.add(paidHolidayDto);

    Assertions.assertDoesNotThrow(() ->
        paidHolidayService.updateHolidaySelects(paidHolidayDtos));
  }

  @Test
  void createPaidHoliday() {
    final PaidHolidayDto paidHolidayDto = new PaidHolidayDto();
    final AuthUser authUser = new AuthUser();
    final User user = new User();
    final PaidHoliday paidHoliday = new PaidHoliday();
    final CompanyPaidHoliday companyPaidHoliday = new CompanyPaidHoliday();

    Mockito.when(userService.findById(Mockito.any())).thenReturn(user);
    Mockito.when(paidHolidayMapper.createFromPaidHolidayDtoAndCreator(Mockito.any(), Mockito.any())).thenReturn(paidHoliday);
    Mockito.when(paidHolidayRepository.save(Mockito.any())).thenReturn(paidHoliday);
    Mockito.when(companyPaidHolidayMapper.createFromPaidHolidayDtoAndPaidHoliday(Mockito.any(), Mockito.any())).thenReturn(companyPaidHoliday);
    Mockito.when(companyPaidHolidayRepository.save(Mockito.any())).thenReturn(companyPaidHoliday);

    Assertions.assertDoesNotThrow(() ->
        paidHolidayService.createPaidHoliday(paidHolidayDto, authUser));
  }

  @Test
  void updatePaidHoliday() {
    final PaidHolidayDto paidHolidayDto = new PaidHolidayDto();
    paidHolidayDto.setId("1");
    paidHolidayDto.setName("007");
    paidHolidayDto.setDate(Timestamp.valueOf(LocalDateTime.now()));

    Assertions.assertDoesNotThrow(() ->
        paidHolidayService.updatePaidHoliday(paidHolidayDto));
  }

  @Test
  void deletePaidHoliday() {
    Assertions.assertDoesNotThrow(() ->
        paidHolidayService.deletePaidHoliday("1"));
  }

  @Test
  void getPaidHolidayEmployees() {
    final List<JobUserDto> jobUserDtos = new ArrayList<>();
    final JobUserDto jobUserDto = new JobUserDto();
    jobUserDto.setId("1");
    jobUserDtos.add(jobUserDto);

    final List<String> filterIds = new ArrayList<>();
    filterIds.add("2");

    final PaidHolidayUser paidHolidayUser = new PaidHolidayUser();

    final List<PaidHolidayUser> newFilterDataSet = new ArrayList<>();
    paidHolidayUser.setSelected(false);
    paidHolidayUser.setUserId("1");

    Mockito.when(userService.findAllJobUsers(Mockito.any())).thenReturn(jobUserDtos);
    Mockito.when(paidHolidayUserRepository.findAllUserIdByCompanyId(Mockito.any())).thenReturn(filterIds);
    Mockito.when(paidHolidayUserRepository.save(Mockito.any())).thenReturn(paidHolidayUser);
    Mockito.when(paidHolidayUserRepository.findAllByCompanyId(Mockito.any())).thenReturn(newFilterDataSet);

    Assertions.assertDoesNotThrow(() ->
        paidHolidayService.getPaidHolidayEmployees("1"));
  }

  @Nested
  class updatePaidHolidayEmployees {
    List<PaidHolidayEmployeeDto> newPaidEmployees;
    PaidHolidayEmployeeDto paidHolidayEmployeeDto;
    List<PaidHolidayUser> employeesStateBefore;
    PaidHolidayUser paidHolidayUser;

    @BeforeEach
    void init() {
      newPaidEmployees = new ArrayList<>();
      paidHolidayEmployeeDto = new PaidHolidayEmployeeDto();
      employeesStateBefore = new ArrayList<>();
      paidHolidayUser = new PaidHolidayUser();
    }

    @Test
    void updatePaidHolidayEmployees_whenContainsId() {
      paidHolidayEmployeeDto.setId("1");
      newPaidEmployees.add(paidHolidayEmployeeDto);

      paidHolidayUser.setUserId("1");
      employeesStateBefore.add(paidHolidayUser);

      Mockito.when(paidHolidayUserRepository.findAllByCompanyId(Mockito.any())).thenReturn(employeesStateBefore);
      Mockito.when(paidHolidayUserRepository.saveAll(Mockito.any())).thenReturn(employeesStateBefore);
      Mockito.when(paidHolidayUserRepository.findByCompanyIdAndUserId(Mockito.any(), Mockito.any())).thenReturn(paidHolidayUser);
      Mockito.when(paidHolidayUserRepository.save(Mockito.any())).thenReturn(paidHolidayUser);

      Assertions.assertDoesNotThrow(() ->
          paidHolidayService.updatePaidHolidayEmployees(newPaidEmployees, "1"));
    }

    @Test
    void updatePaidHolidayEmployees_whenNotContainsId() {
      paidHolidayEmployeeDto.setId("2");
      newPaidEmployees.add(paidHolidayEmployeeDto);

      paidHolidayUser.setUserId("1");
      employeesStateBefore.add(paidHolidayUser);

      Mockito.when(paidHolidayUserRepository.findAllByCompanyId(Mockito.any())).thenReturn(employeesStateBefore);
      Mockito.when(paidHolidayUserRepository.saveAll(Mockito.any())).thenReturn(employeesStateBefore);
      Mockito.when(paidHolidayUserRepository.findByCompanyIdAndUserId(Mockito.any(), Mockito.any())).thenReturn(paidHolidayUser);
      Mockito.when(paidHolidayUserRepository.save(Mockito.any())).thenReturn(paidHolidayUser);

      Assertions.assertDoesNotThrow(() ->
          paidHolidayService.updatePaidHolidayEmployees(newPaidEmployees, "1"));
    }
  }

  @Nested
  class getPaidHoliday {

    @Test
    void whenEmpty_thenShouldThrow() {
      Mockito.when(paidHolidayRepository.findById(Mockito.any())).thenReturn(Optional.empty());

      Assertions.assertThrows(ResourceNotFoundException.class, () -> paidHolidayService.getPaidHoliday("1"));
    }

    @Test
    void whenNotEmpty_thenShouldSuccess() {
      final PaidHoliday paidHoliday = new PaidHoliday();

      Mockito.when(paidHolidayRepository.findById(Mockito.any())).thenReturn(Optional.of(paidHoliday));

      Assertions.assertDoesNotThrow(() -> paidHolidayService.getPaidHoliday("1"));
    }
  }
}