package shamu.company.timeoff.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

  @Autowired
  public PaidHolidayServiceImpl(PaidHolidayRepository paidHolidayRepository,
      CompanyPaidHolidayRepository companyPaidHolidayRepository,
      UserService userService,
      PaidHolidayUserRepository paidHolidayUserRepository) {
    this.paidHolidayRepository = paidHolidayRepository;
    this.companyPaidHolidayRepository = companyPaidHolidayRepository;
    this.userService = userService;
    this.paidHolidayUserRepository = paidHolidayUserRepository;
  }

  @Override
  public void initDefaultPaidHolidays(Company company) {
    //TODO: query by country of company
    List<PaidHoliday> defaultPaidHolidays = paidHolidayRepository.findDefaultPaidHolidays();
    List<CompanyPaidHoliday> companyPaidHolidays = defaultPaidHolidays.stream()
        .map(holiday -> new CompanyPaidHoliday(holiday, company, true))
        .collect(Collectors.toList());
    companyPaidHolidayRepository.saveAll(companyPaidHolidays);
  }

  @Override
  public List<PaidHolidayDto> getPaidHolidays(Long companyId) {
    List<CompanyPaidHoliday> companyPaidHolidays = companyPaidHolidayRepository
        .findAllByCompanyId(companyId);
    return companyPaidHolidays.stream().map(PaidHolidayDto::new)
        .collect(Collectors.toList());
  }

  @Override
  public void updateHolidaySelects(List<PaidHolidayDto> holidaySelectDtos) {
    holidaySelectDtos.forEach(holidaySelectDto ->
        paidHolidayRepository
            .updateHolidaySelect(holidaySelectDto.getId(), holidaySelectDto.getIsSelected())
    );
  }

  @Override
  public PaidHolidayDto createPaidHoliday(PaidHolidayDto paidHolidayDto, Company company) {
    PaidHoliday paidHoliday = paidHolidayDto.covertToNewPaidHolidayEntity(company);
    PaidHoliday paidHolidayReturned = paidHolidayRepository.save(paidHoliday);

    CompanyPaidHoliday companyPaidHoliday = paidHolidayDto
        .covertToNewCompanyPaidHolidayEntity(paidHolidayReturned);
    CompanyPaidHoliday companyPaidHolidayReturned = companyPaidHolidayRepository
        .save(companyPaidHoliday);
    return new PaidHolidayDto(companyPaidHolidayReturned);
  }

  @Override
  public void updatePaidHoliday(PaidHolidayDto paidHolidayDto) {
    paidHolidayRepository.updateDetail(
        paidHolidayDto.getId(), paidHolidayDto.getName(), paidHolidayDto.getDate());
  }

  @Override
  public void deletePaidHoliday(Long id) {
    paidHolidayRepository.delete(id);
  }

  @Override
  public PaidHolidayRelatedUserListDto getPaidHolidayEmployees(Company company) {
    List<JobUserDto> allEmployees = userService.findAllEmployees(company);

    List<PaidHolidayUser> filterDataSet = paidHolidayUserRepository
        .findAllByCompanyId(company.getId());

    List<Long> unSelectedEmployeeIds = filterDataSet.stream()
        .filter(e -> e.isSelected() == false).map(u -> u.getUserId()).collect(Collectors.toList());

    List<JobUserDto> selectedEmployees = allEmployees.stream()
        .filter(u -> !unSelectedEmployeeIds.contains(u.getId())).collect(Collectors.toList());

    List<JobUserDto> unSelectedEmployees = allEmployees.stream()
        .filter(u -> unSelectedEmployeeIds.contains(u.getId())).collect(Collectors.toList());


    return new PaidHolidayRelatedUserListDto(selectedEmployees,unSelectedEmployees);
  }

  @Override
  public void updatePaidHolidayEmployees(List<JobUserDto> newPaidEmployees,Company company) {
    List<Long> paidEmployeeIdsNow = new ArrayList<>();
    newPaidEmployees.stream().map(e -> paidEmployeeIdsNow.add(e.getId()))
        .collect(Collectors.toList());
    List<PaidHolidayUser> employeesStateBefore = paidHolidayUserRepository
        .findAllByCompanyId(company.getId());
    List<Long> employeesIdsBefore = new ArrayList<>();

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
        PaidHolidayUser origin = paidHolidayUserRepository
            .findByCompanyIdAndUserId(company.getId(),u.getId());
        origin.setSelected(true);
        paidHolidayUserRepository.save(origin);
        return;
      }
      PaidHolidayUser newAddedPaidHolidayUser = new PaidHolidayUser(company.getId(),u.getId(),true);
      paidHolidayUserRepository.save(newAddedPaidHolidayUser);
    });

  }
}
