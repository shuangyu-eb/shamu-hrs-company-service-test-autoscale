package shamu.company.timeoff.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.exception.errormapping.AlreadyExistsException;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.helpers.FederalHolidayHelper;
import shamu.company.server.dto.AuthUser;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.dto.PaidHolidayEmployeeDto;
import shamu.company.timeoff.dto.PaidHolidayRelatedUserListDto;
import shamu.company.timeoff.dto.PaidHolidayRelatedUserListMobileDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserDto;
import shamu.company.timeoff.entity.PaidHoliday;
import shamu.company.timeoff.entity.PaidHolidayUser;
import shamu.company.timeoff.entity.mapper.PaidHolidayMapper;
import shamu.company.timeoff.repository.PaidHolidayRepository;
import shamu.company.timeoff.repository.PaidHolidayUserRepository;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;
import shamu.company.utils.ReflectionUtil;

@Service
@Transactional
public class PaidHolidayService {

  private final PaidHolidayRepository paidHolidayRepository;

  private final UserService userService;

  private final PaidHolidayUserRepository paidHolidayUserRepository;

  private final PaidHolidayMapper paidHolidayMapper;

  private final FederalHolidayHelper federalHolidayHelper;

  private final TimeOffPolicyService timeOffPolicyService;

  public static final String PAID_HOLIDAY_EXISTS_MESSAGE = "Paid holiday date already exists";

  @Autowired
  public PaidHolidayService(
      final PaidHolidayRepository paidHolidayRepository,
      final UserService userService,
      final PaidHolidayUserRepository paidHolidayUserRepository,
      final PaidHolidayMapper paidHolidayMapper,
      final FederalHolidayHelper federalHolidayHelper,
      final TimeOffPolicyService timeOffPolicyService) {
    this.paidHolidayRepository = paidHolidayRepository;
    this.userService = userService;
    this.paidHolidayUserRepository = paidHolidayUserRepository;
    this.paidHolidayMapper = paidHolidayMapper;
    this.federalHolidayHelper = federalHolidayHelper;
    this.timeOffPolicyService = timeOffPolicyService;
  }

  public List<PaidHoliday> findAll() {
    return paidHolidayRepository.findAll();
  }

  /** * Get this year, last year and next two years' federal holidays. */
  // PolicyPaidHolidays
  public List<PaidHolidayDto> getPaidHolidays(final AuthUser user) {
    final List<PaidHolidayDto> currentYearPaidHolidays = getCurrentYearPaidHolidays(user);
    currentYearPaidHolidays.addAll(getOtherYearsObservances(currentYearPaidHolidays));
    return currentYearPaidHolidays;
  }

  private List<PaidHolidayDto> getCurrentYearPaidHolidays(final AuthUser user) {
    final List<PaidHoliday> paidHolidays = paidHolidayRepository.findAll();
    return getPaidHolidays(user, paidHolidays);
  }

  private List<PaidHolidayDto> getPaidHolidays(
      final AuthUser user, final List<PaidHoliday> paidHolidays) {
    final List<PaidHolidayDto> list = new ArrayList<>();
    for (final PaidHoliday paidHoliday : paidHolidays) {
      final PaidHolidayDto paidHolidayDto =
          paidHolidayMapper.convertToPaidHolidayDto(paidHoliday, user);
      if (paidHolidayDto.getFederal()) {
        paidHolidayDto.setDate(federalHolidayHelper.timestampOf(paidHolidayDto.getName()));
      }
      list.add(paidHolidayDto);
    }
    return list;
  }

  // UserPaidHolidays
  public List<PaidHolidayDto> getUserPaidHolidays(final AuthUser user, final String userId) {
    final List<PaidHolidayDto> currentYearPaidHolidays =
        getCurrentYearUserPaidHolidays(user, userId);
    currentYearPaidHolidays.addAll(getOtherYearsObservances(currentYearPaidHolidays));
    return currentYearPaidHolidays;
  }

  private List<PaidHolidayDto> getCurrentYearUserPaidHolidays(
      final AuthUser user, final String userId) {
    final PaidHolidayUser paidHolidayUser = paidHolidayUserRepository.findByUserId(userId);
    if (paidHolidayUser == null) {
      return Collections.emptyList();
    }
    final List<PaidHoliday> paidHolidays =
        paidHolidayUser.isSelected() ? paidHolidayRepository.findAll() : new ArrayList<>();
    return getPaidHolidays(user, paidHolidays);
  }

  private List<PaidHolidayDto> getOtherYearsObservances(
      final List<PaidHolidayDto> currentYearPaidHolidays) {
    final int year = Calendar.getInstance().get(Calendar.YEAR);
    final List<PaidHolidayDto> otherYearsObservances = new ArrayList<>();
    currentYearPaidHolidays.forEach(
        paidHolidayDto -> {
          if (paidHolidayDto.getFederal()) {
            otherYearsObservances.add(getNewPaidHolidayDto(paidHolidayDto, year - 1));
            otherYearsObservances.add(getNewPaidHolidayDto(paidHolidayDto, year + 1));
            otherYearsObservances.add(getNewPaidHolidayDto(paidHolidayDto, year + 2));
          }
        });
    return otherYearsObservances;
  }

  private PaidHolidayDto getNewPaidHolidayDto(final PaidHolidayDto paidHolidayDto, final int year) {
    final PaidHolidayDto newPaidHolidayDto = new PaidHolidayDto();
    BeanUtils.copyProperties(paidHolidayDto, newPaidHolidayDto);
    newPaidHolidayDto.setDate(federalHolidayHelper.timestampOf(paidHolidayDto.getName(), year));
    return newPaidHolidayDto;
  }

  public List<PaidHolidayDto> getPaidHolidaysByYear(final AuthUser user, final String year) {
    final List<PaidHolidayDto> paidHolidayDtos = getCurrentYearPaidHolidays(user);
    return paidHolidayDtos.stream()
        .filter(
            paidHolidayDto -> {
              if (paidHolidayDto.getFederal()) {
                paidHolidayDto.setDate(
                    federalHolidayHelper.timestampOf(
                        paidHolidayDto.getName(), Integer.parseInt(year)));
                return true;
              }
              final Timestamp date = paidHolidayDto.getDate();
              final LocalDate localDate = date.toLocalDateTime().toLocalDate();
              final int holidayYear = localDate.getYear();
              return Integer.parseInt(year) == holidayYear;
            })
        .collect(Collectors.toList());
  }

  public List<PaidHolidayDto> getCurrentTwoYearsFederalHolidays(final int currentYear) {
    final List<PaidHolidayDto> paidHolidays = new ArrayList<>();
    paidHolidays.addAll(getFederalHolidaysByYear(currentYear));
    paidHolidays.addAll(getFederalHolidaysByYear(currentYear + 1));
    return paidHolidays;
  }

  public List<PaidHolidayDto> getFederalHolidaysByYear(final int year) {
    final List<PaidHoliday> paidHolidays = paidHolidayRepository.findByFederal(true);
    final List<PaidHolidayDto> paidHolidayDtoList =
        ReflectionUtil.convertTo(paidHolidays, PaidHolidayDto.class);
    paidHolidayDtoList.forEach(
        paidHolidayDto ->
            paidHolidayDto.setDate(
                federalHolidayHelper.timestampOf(paidHolidayDto.getName(), year)));
    return paidHolidayDtoList;
  }

  public void updateHolidaySelects(final List<PaidHolidayDto> holidaySelectDtos) {
    holidaySelectDtos.forEach(
        holidaySelectDto ->
            paidHolidayRepository.updateHolidaySelect(
                holidaySelectDto.getId(), holidaySelectDto.getIsSelected()));
  }

  public void createPaidHoliday(final PaidHolidayDto paidHolidayDto, final AuthUser user) {
    final User creator = userService.findById(user.getId());

    if (isDateDuplicate(paidHolidayDto, user)) {
      throw new AlreadyExistsException(PAID_HOLIDAY_EXISTS_MESSAGE, "paid holiday date");
    }

    final PaidHoliday paidHoliday =
        paidHolidayMapper.createFromPaidHolidayDtoAndCreator(paidHolidayDto, creator);

    paidHolidayRepository.save(paidHoliday);
  }

  public void updatePaidHoliday(final PaidHolidayDto paidHolidayDto, final AuthUser user) {
    if (isDateDuplicate(paidHolidayDto, user)) {
      throw new AlreadyExistsException(PAID_HOLIDAY_EXISTS_MESSAGE, "paid holiday date");
    }
    paidHolidayRepository.updateDetail(
        paidHolidayDto.getId(), paidHolidayDto.getName(), paidHolidayDto.getDate());
  }

  private boolean isDateDuplicate(final PaidHolidayDto paidHolidayDto, final AuthUser user) {
    final List<PaidHolidayDto> paidHolidayDtos = getCurrentYearPaidHolidays(user);
    final String formatDate =
        DateUtil.toLocalDateTime(paidHolidayDto.getDate())
            .format(DateTimeFormatter.ofPattern(DateUtil.SIMPLE_MONTH_DAY_YEAR, Locale.ENGLISH));
    return paidHolidayDtos.stream()
        .anyMatch(
            item ->
                DateUtil.toLocalDateTime(item.getDate())
                        .format(
                            DateTimeFormatter.ofPattern(
                                DateUtil.SIMPLE_MONTH_DAY_YEAR, Locale.ENGLISH))
                        .equals(formatDate)
                    && !item.getId().equals(paidHolidayDto.getId()));
  }

  public void deletePaidHoliday(final String id) {
    paidHolidayRepository.delete(id);
  }

  List<String> saveNewAddedPaidHolidayUserAndGetUnSelectedEmployeeIds(
      final List<TimeOffPolicyRelatedUserDto> allEmployees) {
    final List<String> filterIds = paidHolidayUserRepository.findAllUserId();

    final List<PaidHolidayUser> paidHolidayUsers =
        allEmployees.stream()
            .filter(e -> !filterIds.contains(e.getId().toUpperCase()))
            .map(e -> new PaidHolidayUser(e.getId(), false))
            .collect(Collectors.toList());

    paidHolidayUserRepository.saveAll(paidHolidayUsers);

    final List<PaidHolidayUser> newFilterDataSet =
        paidHolidayUserRepository.findAllPaidHolidayUsers();

    return newFilterDataSet.stream()
        .filter(e -> !e.isSelected())
        .map(PaidHolidayUser::getUserId)
        .collect(Collectors.toList());
  }

  public PaidHolidayRelatedUserListDto getPaidHolidayEmployees() {
    final List<TimeOffPolicyRelatedUserDto> allEmployees =
        timeOffPolicyService.getEmployeesOfNewPolicyOrPaidHoliday();

    final List<String> unSelectedEmployeeIds =
        saveNewAddedPaidHolidayUserAndGetUnSelectedEmployeeIds(allEmployees);

    final List<TimeOffPolicyRelatedUserDto> selectedEmployees =
        allEmployees.stream()
            .filter(u -> !unSelectedEmployeeIds.contains(u.getId()))
            .collect(Collectors.toList());

    final List<TimeOffPolicyRelatedUserDto> unSelectedEmployees =
        allEmployees.stream()
            .filter(u -> unSelectedEmployeeIds.contains(u.getId()))
            .collect(Collectors.toList());

    return new PaidHolidayRelatedUserListDto(selectedEmployees, unSelectedEmployees);
  }

  public PaidHolidayRelatedUserListMobileDto getPaidHolidayEmployeesOnMobile() {
    final List<TimeOffPolicyRelatedUserDto> allEmployees =
        timeOffPolicyService.getEmployeesOfNewPolicyOrPaidHoliday();

    final List<String> unSelectedEmployeeIds =
        saveNewAddedPaidHolidayUserAndGetUnSelectedEmployeeIds(allEmployees);

    final List<TimeOffPolicyRelatedUserDto> selectedEmployees =
        allEmployees.stream()
            .filter(u -> !unSelectedEmployeeIds.contains(u.getId()))
            .collect(Collectors.toList());

    return new PaidHolidayRelatedUserListMobileDto(selectedEmployees, allEmployees);
  }

  public void updatePaidHolidayEmployees(final List<PaidHolidayEmployeeDto> newPaidEmployees) {
    final List<String> paidEmployeeIdsNow =
        newPaidEmployees.stream().map(PaidHolidayEmployeeDto::getId).collect(Collectors.toList());
    final List<PaidHolidayUser> employeesStateBefore =
        paidHolidayUserRepository.findAllPaidHolidayUsers();
    final List<String> employeesIdsBefore = new ArrayList<>();

    employeesStateBefore.forEach(
        u -> {
          employeesIdsBefore.add(u.getUserId());
          if (paidEmployeeIdsNow.contains(u.getUserId())) {
            u.setSelected(true);
            return;
          }
          u.setSelected(false);
        });
    paidHolidayUserRepository.saveAll(employeesStateBefore);

    newPaidEmployees.forEach(
        u -> {
          if (employeesIdsBefore.contains(u.getId())) {
            final PaidHolidayUser origin = paidHolidayUserRepository.findByUserId(u.getId());
            origin.setSelected(true);
            paidHolidayUserRepository.save(origin);
            return;
          }
          final PaidHolidayUser newAddedPaidHolidayUser = new PaidHolidayUser(u.getId(), true);
          paidHolidayUserRepository.save(newAddedPaidHolidayUser);
        });
  }

  public PaidHoliday getPaidHoliday(final String id) {
    return paidHolidayRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Paid holiday with id %s not found!", "id", "paid holiday"));
  }
}
