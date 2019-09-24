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
    return paidHolidayService.getPaidHolidays(getAuthUser());
  }


  @GetMapping(value = "paid-holiday/employees")
  public PaidHolidayRelatedUserListDto getPaidHolidays() {
    return paidHolidayService.getPaidHolidayEmployees(getCompanyId());
  }

  @GetMapping(value = "paid-holiday/employees/count")
  public Integer getPaidHolidaysEmployeesCount() {
    return paidHolidayService
        .getPaidHolidayEmployees(getCompanyId())
        .getPaidHolidaySelectedEmployees().size();
  }

  @PatchMapping(value = "paid-holiday/employees")
  public void updatePaidHolidayEmployees(
      @RequestBody final List<JobUserDto> updatePaidHolidayEmployees) {
    paidHolidayService.updatePaidHolidayEmployees(updatePaidHolidayEmployees, getCompanyId());
  }

  @GetMapping(value = "paid-holidays/years/{year}")
  public List<PaidHolidayDto> getPaidHolidaysByYear(@PathVariable final String year) {
    return paidHolidayService.getPaidHolidaysByYear(getAuthUser(), year);
  }

  @PatchMapping(value = "paid-holidays/select")
  public void updateHolidaySelects(@RequestBody final List<PaidHolidayDto> paidHolidayDtos) {
    paidHolidayService.updateHolidaySelects(paidHolidayDtos);
  }

  @PostMapping(value = "paid-holiday")
  public void createPaidHoliday(
      @RequestBody @Validated final PaidHolidayDto paidHolidayDto) {
    paidHolidayService.createPaidHoliday(paidHolidayDto, getAuthUser());
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
