package shamu.company.timeoff.service.impl;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

@Service
public class PaidHolidayServiceImpl implements PaidHolidayService {

  private final PaidHolidayRepository paidHolidayRepository;

  private final CompanyPaidHolidayRepository companyPaidHolidayRepository;

  private final UserService userService;

  private final PaidHolidayUserRepository paidHolidayUserRepository;

  private final CompanyPaidHolidayMapper companyPaidHolidayMapper;

  private final PaidHolidayMapper paidHolidayMapper;

  @Autowired
  public PaidHolidayServiceImpl(final PaidHolidayRepository paidHolidayRepository,
      final CompanyPaidHolidayRepository companyPaidHolidayRepository,
      final UserService userService,
      final PaidHolidayUserRepository paidHolidayUserRepository,
      final CompanyPaidHolidayMapper companyPaidHolidayMapper,
      final PaidHolidayMapper paidHolidayMapper) {
    this.paidHolidayRepository = paidHolidayRepository;
    this.companyPaidHolidayRepository = companyPaidHolidayRepository;
    this.userService = userService;
    this.paidHolidayUserRepository = paidHolidayUserRepository;
    this.companyPaidHolidayMapper = companyPaidHolidayMapper;
    this.paidHolidayMapper = paidHolidayMapper;
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

  @Override
  public List<PaidHolidayDto> getPaidHolidays(final Long companyId) {
    final List<CompanyPaidHoliday> companyPaidHolidays = companyPaidHolidayRepository
        .findAllByCompanyId(companyId);
    return companyPaidHolidays.stream().map(companyPaidHolidayMapper::convertToPaidHolidayDto)
        .collect(Collectors.toList());
  }

  @Override
  public List<PaidHolidayDto> getPaidHolidaysByYear(final Long companyId, final String year) {
    final List<PaidHolidayDto> paidHolidayDtos = getPaidHolidays(companyId);
    return paidHolidayDtos.stream().filter(paidHolidayDto -> {
      Timestamp date = paidHolidayDto.getDate();
      if (date == null) {
        return true;
      }
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
      final Company company) {
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
  public PaidHolidayRelatedUserListDto getPaidHolidayEmployees(final Company company) {
    final List<JobUserDto> allEmployees = userService.findAllJobUsers(company);

    final List<PaidHolidayUser> filterDataSet = paidHolidayUserRepository
        .findAllByCompanyId(company.getId());

    final List<Long> filterIds = filterDataSet.stream()
        .map(d -> d.getUserId()).collect(Collectors.toList());

    allEmployees.stream().forEach(e -> {
      if (!filterIds.contains(e.getId())) {
        final PaidHolidayUser newAddedPaidHolidayUser = new PaidHolidayUser(company.getId(),
            e.getId(),
            false);
        paidHolidayUserRepository.save(newAddedPaidHolidayUser);
      }
    });

    final List<PaidHolidayUser> newFilterDataSet = paidHolidayUserRepository
        .findAllByCompanyId(company.getId());

    final List<Long> unSelectedEmployeeIds = newFilterDataSet.stream()
        .filter(e -> e.isSelected() == false).map(u -> u.getUserId()).collect(Collectors.toList());

    final List<JobUserDto> selectedEmployees = allEmployees.stream()
        .filter(u -> !unSelectedEmployeeIds.contains(u.getId())).collect(Collectors.toList());

    final List<JobUserDto> unSelectedEmployees = allEmployees.stream()
        .filter(u -> unSelectedEmployeeIds.contains(u.getId())).collect(Collectors.toList());

    return new PaidHolidayRelatedUserListDto(selectedEmployees, unSelectedEmployees);
  }

  @Override
  public void updatePaidHolidayEmployees(final List<JobUserDto> newPaidEmployees,
      final Company company) {
    final List<Long> paidEmployeeIdsNow = new ArrayList<>();
    newPaidEmployees.stream().map(e -> paidEmployeeIdsNow.add(e.getId()))
        .collect(Collectors.toList());
    final List<PaidHolidayUser> employeesStateBefore = paidHolidayUserRepository
        .findAllByCompanyId(company.getId());
    final List<Long> employeesIdsBefore = new ArrayList<>();

    employeesStateBefore.stream().forEach(u -> {
      employeesIdsBefore.add(u.getUserId());
      if (paidEmployeeIdsNow.contains(u.getUserId())) {
        u.setSelected(true);
        return;
      }
      u.setSelected(false);
    });
    paidHolidayUserRepository.saveAll(employeesStateBefore);

    newPaidEmployees.stream().forEach(u -> {
      if (employeesIdsBefore.contains(u.getId())) {
        final PaidHolidayUser origin = paidHolidayUserRepository
            .findByCompanyIdAndUserId(company.getId(), u.getId());
        origin.setSelected(true);
        paidHolidayUserRepository.save(origin);
        return;
      }
      final PaidHolidayUser newAddedPaidHolidayUser = new PaidHolidayUser(company.getId(),
          u.getId(),
          true);
      paidHolidayUserRepository.save(newAddedPaidHolidayUser);
    });

  }
}
