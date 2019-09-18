package shamu.company.timeoff.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestApiController
public class PaidHolidayRestController extends BaseRestController {

  private final PaidHolidayService paidHolidayService;

  @Autowired
  public PaidHolidayRestController(final PaidHolidayService paidHolidayService) {
    this.paidHolidayService = paidHolidayService;
  }

  @GetMapping(value = "paid-holidays")
  public List<PaidHolidayDto> getAllPaidHolidays() {
    final Company company = getCompany();
    return paidHolidayService.getPaidHolidays(company.getId());
  }


  @GetMapping(value = "paid-holiday/employees")
  public PaidHolidayRelatedUserListDto getPaidHolidays() {
    final Company company = getCompany();
    return paidHolidayService.getPaidHolidayEmployees(company);
  }

  @GetMapping(value = "paid-holiday/employees/count")
  public Integer getPaidHolidaysEmployeesCount() {
    final Company company = getCompany();
    return paidHolidayService
        .getPaidHolidayEmployees(company)
        .getPaidHolidaySelectedEmployees().size();
  }

  @PatchMapping(value = "paid-holiday/employees")
  public void updatePaidHolidayEmployees(
      @RequestBody final List<JobUserDto> updatePaidHolidayEmployees) {
    final Company company = getCompany();
    paidHolidayService.updatePaidHolidayEmployees(updatePaidHolidayEmployees, company);
  }

  @GetMapping(value = "paid-holidays/years/{year}")
  public List<PaidHolidayDto> getPaidHolidaysByYear(@PathVariable final String year) {
    final Company company = getCompany();
    return paidHolidayService.getPaidHolidaysByYear(company.getId(), year);
  }

  @PatchMapping(value = "paid-holidays/select")
  public void updateHolidaySelects(@RequestBody final List<PaidHolidayDto> paidHolidayDtos) {
    paidHolidayService.updateHolidaySelects(paidHolidayDtos);
  }

  @PostMapping(value = "paid-holiday")
  public void createPaidHoliday(
      @RequestBody final PaidHolidayDto paidHolidayDto) {
    final Company company = getCompany();
    paidHolidayService.createPaidHoliday(paidHolidayDto, company);
  }

  @PatchMapping(value = "paid-holidays")
  @PreAuthorize("hasPermission(#paidHolidayDto.id, 'PAID_HOLIDAY', 'EDIT_PAID_HOLIDAY')")
  public void updatePaidHoliday(
      @RequestBody @Validated final PaidHolidayDto paidHolidayDto) {
    paidHolidayService.updatePaidHoliday(paidHolidayDto);
  }

  @DeleteMapping(value = "paid-holidays/{id}")
  @PreAuthorize("hasPermission(#id, 'PAID_HOLIDAY', 'DELETE_PAID_HOLIDAY')")
  public void updatePaidHoliday(@HashidsFormat @PathVariable final Long id) {
    paidHolidayService.deletePaidHoliday(id);
  }


}
