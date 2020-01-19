package shamu.company.timeoff.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.dto.PaidHolidayEmployeeDto;
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
    return paidHolidayService.getPaidHolidays(findAuthUser());
  }

  @GetMapping(value = "paid-holidays/user/{targetUserId}")
  public List<PaidHolidayDto> getUserAllPaidHolidays(
          @PathVariable final String targetUserId) {
    return paidHolidayService.getUserPaidHolidays(findAuthUser(), targetUserId);
  }


  @GetMapping(value = "paid-holidays/employees")
  public PaidHolidayRelatedUserListDto getPaidHolidays() {
    return paidHolidayService.getPaidHolidayEmployees(findCompanyId());
  }

  @GetMapping(value = "paid-holidays/employees/count")
  public Integer getPaidHolidaysEmployeesCount() {
    return paidHolidayService
        .getPaidHolidayEmployees(findCompanyId())
        .getPaidHolidaySelectedEmployees().size();
  }

  @PatchMapping(value = "paid-holidays/employees")
  @PreAuthorize("hasPermission(#updatePaidHolidayEmployees, 'PAID_HOLIDAY_USER', 'EDIT_USER')")
  public HttpEntity updatePaidHolidayEmployees(
      @RequestBody final List<PaidHolidayEmployeeDto> updatePaidHolidayEmployees) {
    paidHolidayService.updatePaidHolidayEmployees(updatePaidHolidayEmployees, findCompanyId());
    return new ResponseEntity<>(HttpStatus.OK);

  }

  @GetMapping(value = "paid-holidays/years/{year}")
  public List<PaidHolidayDto> getPaidHolidaysByYear(@PathVariable final String year) {
    return paidHolidayService.getPaidHolidaysByYear(findAuthUser(), year);
  }

  @PatchMapping(value = "paid-holidays/select")
  @PreAuthorize("hasPermission(#paidHolidayDtos, 'COMPANY_PAID_HOLIDAY', 'EDIT_PAID_HOLIDAY')")
  public HttpEntity updateHolidaySelects(@RequestBody final List<PaidHolidayDto> paidHolidayDtos) {
    paidHolidayService.updateHolidaySelects(paidHolidayDtos);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping(value = "paid-holidays")
  @PreAuthorize("hasAuthority('EDIT_PAID_HOLIDAY')")
  public HttpEntity createPaidHoliday(
      @RequestBody @Validated final PaidHolidayDto paidHolidayDto) {
    paidHolidayService.createPaidHoliday(paidHolidayDto, findAuthUser());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PatchMapping(value = "paid-holidays/{id}")
  @PreAuthorize("hasPermission(#paidHolidayDto.id, 'PAID_HOLIDAY', 'EDIT_PAID_HOLIDAY')")
  public HttpEntity updatePaidHoliday(
      @RequestBody @Validated final PaidHolidayDto paidHolidayDto) {
    paidHolidayService.updatePaidHoliday(paidHolidayDto);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @DeleteMapping(value = "paid-holidays/{id}")
  @PreAuthorize("hasPermission(#id, 'PAID_HOLIDAY', 'DELETE_PAID_HOLIDAY')")
  public HttpEntity updatePaidHoliday(@PathVariable final String id) {
    paidHolidayService.deletePaidHoliday(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }


}
