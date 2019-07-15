package shamu.company.timeoff.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
  public PaidHolidayRestController(UserService userService, PaidHolidayService paidHolidayService) {
    this.userService = userService;
    this.paidHolidayService = paidHolidayService;
  }

  @GetMapping(value = "users/{userId}/paid-holidays")
  public List<PaidHolidayDto> getPaidHolidays(@HashidsFormat @PathVariable Long userId) {
    User user = userService.findUserById(userId);
    Company company = user.getCompany();
    return paidHolidayService.getPaidHolidays(company.getId());
  }


  @GetMapping(value = "paid-holiday/employees")
  public PaidHolidayRelatedUserListDto getPaidHolidays() {
    Company company = this.getCompany();
    return paidHolidayService.getPaidHolidayEmployees(company);
  }

  @PatchMapping(value = "paid-holiday/employees")
  public void updatePaidHolidayEmployees(@RequestBody List<JobUserDto> updatePaidHolidayEmployees) {
    Company company = this.getCompany();
    paidHolidayService.updatePaidHolidayEmployees(updatePaidHolidayEmployees, company);
  }

  @PostMapping(value = "paid-holidays/default")
  public void createInitialPaidHolidays(@RequestBody String workEmail) {
    User user = userService.findUserByEmail(workEmail);
    Company company = user.getCompany();
    paidHolidayService.initDefaultPaidHolidays(company);
  }

  @PatchMapping(value = "paid-holidays/select")
  public void updateHolidaySelects(@RequestBody List<PaidHolidayDto> paidHolidayDtos) {
    paidHolidayService.updateHolidaySelects(paidHolidayDtos);
  }

  @PostMapping(value = "users/{userId}/paid-holiday")
  public PaidHolidayDto createPaidHoliday(
      @HashidsFormat @PathVariable Long userId,
      @RequestBody PaidHolidayDto paidHolidayDto) {
    User user = userService.findUserById(userId);
    Company company = user.getCompany();
    return paidHolidayService.createPaidHoliday(paidHolidayDto, company);
  }

  @PatchMapping(value = "paid-holidays")
  public void updatePaidHoliday(@RequestBody PaidHolidayDto paidHolidayDto) {
    paidHolidayService.updatePaidHoliday(paidHolidayDto);
  }

  @DeleteMapping(value = "paid-holidays/{id}")
  public void updatePaidHoliday(@HashidsFormat @PathVariable Long id) {
    paidHolidayService.deletePaidHoliday(id);
  }
}
