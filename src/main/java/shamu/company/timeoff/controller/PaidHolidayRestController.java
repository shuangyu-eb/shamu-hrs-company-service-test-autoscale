package shamu.company.timeoff.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.company.entity.Company;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.dto.JobUserDto;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.dto.PaidHolidayRelatedUserListDto;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@RestApiController
public class PaidHolidayRestController extends BaseRestController {

  private final UserService userService;

  private final PaidHolidayService paidHolidayService;

  @Autowired
  public PaidHolidayRestController(final UserService userService,
      final PaidHolidayService paidHolidayService) {
    this.userService = userService;
    this.paidHolidayService = paidHolidayService;
  }

  @GetMapping(value = "users/{userId}/paid-holidays")
  public List<PaidHolidayDto> getPaidHolidays(@HashidsFormat @PathVariable final Long userId) {
    final User user = userService.findUserById(userId);
    final Company company = user.getCompany();
    return paidHolidayService.getPaidHolidays(company.getId());
  }


  @GetMapping(value = "paid-holiday/employees")
  public PaidHolidayRelatedUserListDto getPaidHolidays() {
    final Company company = this.getCompany();
    return paidHolidayService.getPaidHolidayEmployees(company);
  }

  @GetMapping(value = "paid-holiday/employees/count")
  public Integer getPaidHolidaysEmployeesCount() {
    final Company company = this.getCompany();
    return paidHolidayService
        .getPaidHolidayEmployees(company)
        .getPaidHolidaySelectedEmployees().size();
  }

  @PatchMapping(value = "paid-holiday/employees")
  public void updatePaidHolidayEmployees(
      @RequestBody final List<JobUserDto> updatePaidHolidayEmployees) {
    final Company company = this.getCompany();
    paidHolidayService.updatePaidHolidayEmployees(updatePaidHolidayEmployees, company);
  }

  @GetMapping(value = "users/{id}/paid-holidays/years/{year}")
  public List<PaidHolidayDto> getPaidHolidaysByYear(@HashidsFormat @PathVariable final Long id,
      @PathVariable final String year) {
    final User user = userService.findUserById(id);
    final Company company = user.getCompany();
    return paidHolidayService.getPaidHolidaysByYear(company.getId(), year);
  }

  @PostMapping(value = "paid-holidays/default")
  public void createInitialPaidHolidays(@RequestBody final String workEmail) {
    final User user = userService.findUserByEmail(workEmail);
    final Company company = user.getCompany();
    paidHolidayService.initDefaultPaidHolidays(company);
  }

  @PatchMapping(value = "paid-holidays/select")
  public void updateHolidaySelects(@RequestBody final List<PaidHolidayDto> paidHolidayDtos) {
    paidHolidayService.updateHolidaySelects(paidHolidayDtos);
  }

  @PostMapping(value = "users/{userId}/paid-holiday")
  public void createPaidHoliday(
      @HashidsFormat @PathVariable final Long userId,
      @RequestBody final PaidHolidayDto paidHolidayDto) {
    final User user = userService.findUserById(userId);
    final Company company = user.getCompany();
    paidHolidayService.createPaidHoliday(paidHolidayDto, company);
  }

  @PatchMapping(value = "paid-holidays")
  public void updatePaidHoliday(
      @RequestBody @Validated final PaidHolidayDto paidHolidayDto) {
    paidHolidayService.updatePaidHoliday(paidHolidayDto);
  }

  @DeleteMapping(value = "paid-holidays/{id}")
  public void updatePaidHoliday(@HashidsFormat @PathVariable final Long id) {
    paidHolidayService.deletePaidHoliday(id);
  }


}
