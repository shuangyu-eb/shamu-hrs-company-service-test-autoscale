package shamu.company.timeoff.service.impl;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.company.CompanyService;
import shamu.company.company.entity.Company;
import shamu.company.job.dto.JobUserDto;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.dto.PaidHolidayRelatedUserListDto;
import shamu.company.timeoff.entity.CompanyPaidHoliday;
import shamu.company.timeoff.entity.PaidHoliday;
import shamu.company.timeoff.entity.PaidHolidayUser;
import shamu.company.timeoff.entity.mapper.CompanyPaidHolidayMapper;
import shamu.company.timeoff.entity.mapper.PaidHolidayMapper;
import shamu.company.timeoff.repository.CompanyPaidHolidayRepository;
import shamu.company.timeoff.repository.PaidHolidayRepository;
import shamu.company.timeoff.repository.PaidHolidayUserRepository;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.user.service.UserService;
import shamu.company.utils.FederalHolidays;

@Service
@Transactional
public class PaidHolidayServiceImpl implements PaidHolidayService {

  private final PaidHolidayRepository paidHolidayRepository;

  private final CompanyPaidHolidayRepository companyPaidHolidayRepository;

  private final UserService userService;

  private final PaidHolidayUserRepository paidHolidayUserRepository;

  private final CompanyPaidHolidayMapper companyPaidHolidayMapper;

  private final PaidHolidayMapper paidHolidayMapper;

  private final FederalHolidays federalHolidays;

  private final CompanyService companyService;

  @Autowired
  public PaidHolidayServiceImpl(final PaidHolidayRepository paidHolidayRepository,
      final CompanyPaidHolidayRepository companyPaidHolidayRepository,
      final UserService userService,
      final PaidHolidayUserRepository paidHolidayUserRepository,
      final CompanyPaidHolidayMapper companyPaidHolidayMapper,
      final PaidHolidayMapper paidHolidayMapper,
      final FederalHolidays federalHolidays,
      final CompanyService companyService) {
    this.paidHolidayRepository = paidHolidayRepository;
    this.companyPaidHolidayRepository = companyPaidHolidayRepository;
    this.userService = userService;
    this.paidHolidayUserRepository = paidHolidayUserRepository;
    this.companyPaidHolidayMapper = companyPaidHolidayMapper;
    this.paidHolidayMapper = paidHolidayMapper;
    this.federalHolidays = federalHolidays;
    this.companyService = companyService;
  }

  @Override
  public void initDefaultPaidHolidays(final Company company) {
    //TODO: query by country of company
    final List<PaidHoliday> defaultPaidHolidays = paidHolidayRepository.findDefaultPaidHolidays();
    final List<CompanyPaidHoliday> companyPaidHolidays = defaultPaidHolidays.stream()
        .map(holiday -> new CompanyPaidHoliday(holiday, company, true))
        .collect(Collectors.toList());
    companyPaidHolidayRepository.saveAll(companyPaidHolidays);
  }

  private List<PaidHolidayDto> getCurrentYearPaidHolidays(final Long companyId) {
    final List<CompanyPaidHoliday> companyPaidHolidays = companyPaidHolidayRepository
        .findAllByCompanyId(companyId);
    return companyPaidHolidays.stream()
        .map(companyPaidHolidayMapper::convertToPaidHolidayDto)
        .peek(paidHolidayDto -> {
          if (paidHolidayDto.getFederal()) {
            Date observance = federalHolidays.dateOf(paidHolidayDto.getName());
            paidHolidayDto.setDate(new Timestamp(observance.getTime()));
          }
        })
        .collect(Collectors.toList());
  }

  private PaidHolidayDto getNewPaidHolidayDto(final PaidHolidayDto paidHolidayDto, final int year) {
    final Timestamp observanceYear = new Timestamp(
        federalHolidays.dateOf(paidHolidayDto.getName(), year).getTime());
    final PaidHolidayDto newPaidHolidayDto = new PaidHolidayDto();
    BeanUtils.copyProperties(paidHolidayDto, newPaidHolidayDto);
    newPaidHolidayDto.setDate(observanceYear);
    return newPaidHolidayDto;
  }

  /***
   * Get this year, last year and next two years' federal holidays.
   */
  @Override
  public List<PaidHolidayDto> getPaidHolidays(final Long companyId) {
    final List<PaidHolidayDto> currentYearPaidHolidays = getCurrentYearPaidHolidays(companyId);
    final int year = Calendar.getInstance().get(Calendar.YEAR);
    final List<PaidHolidayDto> otherYearsObservances = new ArrayList<>();
    currentYearPaidHolidays.forEach(paidHolidayDto -> {
      if (paidHolidayDto.getFederal()) {
        otherYearsObservances.add(getNewPaidHolidayDto(paidHolidayDto, year - 1));
        otherYearsObservances.add(getNewPaidHolidayDto(paidHolidayDto, year + 1));
        otherYearsObservances.add(getNewPaidHolidayDto(paidHolidayDto, year + 2));
      }
    });
    currentYearPaidHolidays.addAll(otherYearsObservances);
    return currentYearPaidHolidays;
  }

  @Override
  public List<PaidHolidayDto> getPaidHolidaysByYear(final Long companyId, final String year) {
    final List<PaidHolidayDto> paidHolidayDtos = getCurrentYearPaidHolidays(companyId);
    return paidHolidayDtos.stream().filter(paidHolidayDto -> {
      if (paidHolidayDto.getFederal()) {
        Date observance = federalHolidays.dateOf(paidHolidayDto.getName(), Integer.valueOf(year));
        paidHolidayDto.setDate(new Timestamp(observance.getTime()));
        return true;
      }
      Timestamp date = paidHolidayDto.getDate();
      LocalDate localDate = date.toLocalDateTime().toLocalDate();
      int holidayYear = localDate.getYear();
      return Integer.valueOf(year) == holidayYear;
    }).collect(Collectors.toList());
  }

  @Override
  public void updateHolidaySelects(final List<PaidHolidayDto> holidaySelectDtos) {
    holidaySelectDtos.forEach(holidaySelectDto ->
        paidHolidayRepository
            .updateHolidaySelect(holidaySelectDto.getId(), holidaySelectDto.getIsSelected())
    );
  }

  @Override
  public void createPaidHoliday(final PaidHolidayDto paidHolidayDto,
      final Long companyId) {
    final Company company = companyService.findById(companyId);
    final PaidHoliday paidHoliday = paidHolidayMapper
        .createFromPaidHolidayDtoAndCompany(paidHolidayDto, company);
    final PaidHoliday paidHolidayReturned = paidHolidayRepository.save(paidHoliday);

    final CompanyPaidHoliday companyPaidHoliday = companyPaidHolidayMapper
        .createFromPaidHolidayDtoAndPaidHoliday(paidHolidayDto, paidHolidayReturned);
    companyPaidHolidayRepository.save(companyPaidHoliday);
  }

  @Override
  public void updatePaidHoliday(final PaidHolidayDto paidHolidayDto) {
    paidHolidayRepository.updateDetail(
        paidHolidayDto.getId(), paidHolidayDto.getName(), paidHolidayDto.getDate());
  }

  @Override
  public void deletePaidHoliday(final Long id) {
    paidHolidayRepository.delete(id);
    companyPaidHolidayRepository.deleteByPaidHolidayId(id);
  }

  @Override
  public PaidHolidayRelatedUserListDto getPaidHolidayEmployees(final Long companyId) {
    final List<JobUserDto> allEmployees = userService.findAllJobUsers(companyId);

    final List<Long> filterIds = paidHolidayUserRepository
        .findAllUserIdByCompanyId(companyId).stream().map(BigInteger::longValue).collect(
            Collectors.toList());

    allEmployees.forEach(e -> {
      if (!filterIds.contains(e.getId())) {
        final PaidHolidayUser newAddedPaidHolidayUser = new PaidHolidayUser(companyId,
            e.getId(),
            false);
        paidHolidayUserRepository.save(newAddedPaidHolidayUser);
      }
    });

    final List<PaidHolidayUser> newFilterDataSet = paidHolidayUserRepository
        .findAllByCompanyId(companyId);

    final List<Long> unSelectedEmployeeIds = newFilterDataSet.stream()
        .filter(e -> !e.isSelected()).map(PaidHolidayUser::getUserId).collect(Collectors.toList());

    final List<JobUserDto> selectedEmployees = allEmployees.stream()
        .filter(u -> !unSelectedEmployeeIds.contains(u.getId())).collect(Collectors.toList());

    final List<JobUserDto> unSelectedEmployees = allEmployees.stream()
        .filter(u -> unSelectedEmployeeIds.contains(u.getId())).collect(Collectors.toList());

    return new PaidHolidayRelatedUserListDto(selectedEmployees, unSelectedEmployees);
  }

  @Override
  public void updatePaidHolidayEmployees(final List<JobUserDto> newPaidEmployees,
      final Long companyId) {
    final List<Long> paidEmployeeIdsNow = newPaidEmployees.stream().map(JobUserDto::getId)
        .collect(Collectors.toList());
    final List<PaidHolidayUser> employeesStateBefore = paidHolidayUserRepository
        .findAllByCompanyId(companyId);
    final List<Long> employeesIdsBefore = new ArrayList<>();

    employeesStateBefore.forEach(u -> {
      employeesIdsBefore.add(u.getUserId());
      if (paidEmployeeIdsNow.contains(u.getUserId())) {
        u.setSelected(true);
        return;
      }
      u.setSelected(false);
    });
    paidHolidayUserRepository.saveAll(employeesStateBefore);

    newPaidEmployees.forEach(u -> {
      if (employeesIdsBefore.contains(u.getId())) {
        final PaidHolidayUser origin = paidHolidayUserRepository
            .findByCompanyIdAndUserId(companyId, u.getId());
        origin.setSelected(true);
        paidHolidayUserRepository.save(origin);
        return;
      }
      final PaidHolidayUser newAddedPaidHolidayUser = new PaidHolidayUser(companyId,
          u.getId(),
          true);
      paidHolidayUserRepository.save(newAddedPaidHolidayUser);
    });

  }

  @Override
  public PaidHoliday getPaidHoliday(final Long id) {
    return paidHolidayRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Paid holiday was not found"));
  }
}
