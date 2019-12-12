package shamu.company.timeoff.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.BaseAuthorityDto;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.company.entity.Company;
import shamu.company.helpers.FederalHolidayHelper;
import shamu.company.job.dto.JobUserDto;
import shamu.company.server.dto.AuthUser;
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
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@Service
@Transactional
public class PaidHolidayService {

  private final PaidHolidayRepository paidHolidayRepository;

  private final CompanyPaidHolidayRepository companyPaidHolidayRepository;

  private final UserService userService;

  private final PaidHolidayUserRepository paidHolidayUserRepository;

  private final CompanyPaidHolidayMapper companyPaidHolidayMapper;

  private final PaidHolidayMapper paidHolidayMapper;

  private final FederalHolidayHelper federalHolidayHelper;

  @Autowired
  public PaidHolidayService(final PaidHolidayRepository paidHolidayRepository,
      final CompanyPaidHolidayRepository companyPaidHolidayRepository,
      final UserService userService,
      final PaidHolidayUserRepository paidHolidayUserRepository,
      final CompanyPaidHolidayMapper companyPaidHolidayMapper,
      final PaidHolidayMapper paidHolidayMapper,
      final FederalHolidayHelper federalHolidayHelper) {
    this.paidHolidayRepository = paidHolidayRepository;
    this.companyPaidHolidayRepository = companyPaidHolidayRepository;
    this.userService = userService;
    this.paidHolidayUserRepository = paidHolidayUserRepository;
    this.companyPaidHolidayMapper = companyPaidHolidayMapper;
    this.paidHolidayMapper = paidHolidayMapper;
    this.federalHolidayHelper = federalHolidayHelper;
  }

  public void initDefaultPaidHolidays(final Company company) {
    //TODO: query by country of company
    final List<PaidHoliday> defaultPaidHolidays = paidHolidayRepository.findDefaultPaidHolidays();
    final List<CompanyPaidHoliday> companyPaidHolidays = defaultPaidHolidays.stream()
        .map(holiday -> new CompanyPaidHoliday(holiday, company, true))
        .collect(Collectors.toList());
    companyPaidHolidayRepository.saveAll(companyPaidHolidays);
  }

  private List<PaidHolidayDto> getCurrentYearPaidHolidays(final AuthUser user) {
    final List<CompanyPaidHoliday> companyPaidHolidays;
    companyPaidHolidays = companyPaidHolidayRepository
            .findAllByCompanyId(user.getCompanyId());
    return getPaidHolidayFromCompany(user, companyPaidHolidays);
  }

  private List<PaidHolidayDto> getCurrentYearUserPaidHolidays(
          final AuthUser user, final String userId) {
    final List<CompanyPaidHoliday> companyPaidHolidays;
    companyPaidHolidays = companyPaidHolidayRepository
            .findAllByCompanyIdAndUserId(user.getCompanyId(), userId);
    return getPaidHolidayFromCompany(user, companyPaidHolidays);
  }

  private List<PaidHolidayDto> getPaidHolidayFromCompany(
          final AuthUser user, final List<CompanyPaidHoliday> companyPaidHolidays) {
    return companyPaidHolidays.stream()
            .map(paidHoliday -> companyPaidHolidayMapper.convertToPaidHolidayDto(paidHoliday, user))
            .peek(paidHolidayDto -> {
              if (paidHolidayDto.getFederal()) {
                paidHolidayDto.setDate(federalHolidayHelper.timestampOf(paidHolidayDto.getName()));
              }
            })
            .collect(Collectors.toList());
  }

  private PaidHolidayDto getNewPaidHolidayDto(final PaidHolidayDto paidHolidayDto, final int year) {
    final PaidHolidayDto newPaidHolidayDto = new PaidHolidayDto();
    BeanUtils.copyProperties(paidHolidayDto, newPaidHolidayDto);
    newPaidHolidayDto.setDate(federalHolidayHelper.timestampOf(paidHolidayDto.getName(), year));
    return newPaidHolidayDto;
  }

  /***
   * Get this year, last year and next two years' federal holidays.
   */
  // PolicyPaidHolidays
  public List<PaidHolidayDto> getPaidHolidays(final AuthUser user) {
    final List<PaidHolidayDto> currentYearPaidHolidays = getCurrentYearPaidHolidays(user);
    currentYearPaidHolidays.addAll(getOtherYearsObservances(currentYearPaidHolidays));
    return currentYearPaidHolidays;
  }

  // UserPaidHolidays
  public List<PaidHolidayDto> getUserPaidHolidays(final AuthUser user, final String userId) {
    final List<PaidHolidayDto> currentYearPaidHolidays
            = getCurrentYearUserPaidHolidays(user, userId);
    currentYearPaidHolidays.addAll(getOtherYearsObservances(currentYearPaidHolidays));
    return currentYearPaidHolidays;
  }

  private List<PaidHolidayDto> getOtherYearsObservances(
          final List<PaidHolidayDto> currentYearPaidHolidays) {
    final int year = Calendar.getInstance().get(Calendar.YEAR);
    final List<PaidHolidayDto> otherYearsObservances = new ArrayList<>();
    currentYearPaidHolidays.forEach(paidHolidayDto -> {
      if (paidHolidayDto.getFederal()) {
        otherYearsObservances.add(getNewPaidHolidayDto(paidHolidayDto, year - 1));
        otherYearsObservances.add(getNewPaidHolidayDto(paidHolidayDto, year + 1));
        otherYearsObservances.add(getNewPaidHolidayDto(paidHolidayDto, year + 2));
      }
    });
    return otherYearsObservances;
  }

  public List<PaidHolidayDto> getPaidHolidaysByYear(final AuthUser user, final String year) {
    final List<PaidHolidayDto> paidHolidayDtos = getCurrentYearPaidHolidays(user);
    return paidHolidayDtos.stream().filter(paidHolidayDto -> {
      if (paidHolidayDto.getFederal()) {
        paidHolidayDto.setDate(
            federalHolidayHelper.timestampOf(paidHolidayDto.getName(), Integer.valueOf(year)));
        return true;
      }
      Timestamp date = paidHolidayDto.getDate();
      LocalDate localDate = date.toLocalDateTime().toLocalDate();
      int holidayYear = localDate.getYear();
      return Integer.valueOf(year) == holidayYear;
    }).collect(Collectors.toList());
  }

  public void updateHolidaySelects(final List<PaidHolidayDto> holidaySelectDtos) {
    holidaySelectDtos.forEach(holidaySelectDto ->
        paidHolidayRepository
            .updateHolidaySelect(holidaySelectDto.getId(), holidaySelectDto.getIsSelected())
    );
  }

  public void createPaidHoliday(final PaidHolidayDto paidHolidayDto,
      final AuthUser user) {
    final User creator = userService.findById(user.getId());

    final PaidHoliday paidHoliday = paidHolidayMapper
        .createFromPaidHolidayDtoAndCreator(paidHolidayDto, creator);
    final PaidHoliday paidHolidayReturned = paidHolidayRepository.save(paidHoliday);

    final CompanyPaidHoliday companyPaidHoliday = companyPaidHolidayMapper
        .createFromPaidHolidayDtoAndPaidHoliday(paidHolidayDto, paidHolidayReturned);
    companyPaidHolidayRepository.save(companyPaidHoliday);
  }

  public void updatePaidHoliday(final PaidHolidayDto paidHolidayDto) {
    paidHolidayRepository.updateDetail(
        paidHolidayDto.getId(), paidHolidayDto.getName(), paidHolidayDto.getDate());
  }

  public void deletePaidHoliday(final String id) {
    paidHolidayRepository.delete(id);
    companyPaidHolidayRepository.deleteByPaidHolidayId(id);
  }

  public PaidHolidayRelatedUserListDto getPaidHolidayEmployees(final String companyId) {
    final List<JobUserDto> allEmployees = userService.findAllJobUsers(companyId);

    final List<String> filterIds = paidHolidayUserRepository
            .findAllUserIdByCompanyId(companyId);

    allEmployees.forEach(e -> {
      if (!filterIds.contains(e.getId().toUpperCase())) {
        final PaidHolidayUser newAddedPaidHolidayUser = new PaidHolidayUser(companyId,
            e.getId(),
            false);
        paidHolidayUserRepository.save(newAddedPaidHolidayUser);
      }
    });

    final List<PaidHolidayUser> newFilterDataSet = paidHolidayUserRepository
        .findAllByCompanyId(companyId);

    final List<String> unSelectedEmployeeIds = newFilterDataSet.stream()
        .filter(e -> !e.isSelected()).map(PaidHolidayUser::getUserId).collect(Collectors.toList());

    final List<JobUserDto> selectedEmployees = allEmployees.stream()
        .filter(u -> !unSelectedEmployeeIds.contains(u.getId())).collect(Collectors.toList());

    final List<JobUserDto> unSelectedEmployees = allEmployees.stream()
        .filter(u -> unSelectedEmployeeIds.contains(u.getId())).collect(Collectors.toList());

    return new PaidHolidayRelatedUserListDto(selectedEmployees, unSelectedEmployees);
  }

  public void updatePaidHolidayEmployees(final List<BaseAuthorityDto> newPaidEmployees,
      final String companyId) {
    final List<String> paidEmployeeIdsNow = newPaidEmployees.stream().map(
        BaseAuthorityDto::getId)
        .collect(Collectors.toList());
    final List<PaidHolidayUser> employeesStateBefore = paidHolidayUserRepository
        .findAllByCompanyId(companyId);
    final List<String> employeesIdsBefore = new ArrayList<>();

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

  public PaidHoliday getPaidHoliday(final String id) {
    return paidHolidayRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Paid holiday was not found"));
  }
}
