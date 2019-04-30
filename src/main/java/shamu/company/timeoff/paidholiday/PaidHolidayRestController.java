package shamu.company.timeoff.paidholiday;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.company.entity.Company;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@RestApiController
public class PaidHolidayRestController {

  private final UserService userService;

  private final PaidHolidayService paidHolidayService;

  @Autowired
  public PaidHolidayRestController(UserService userService, PaidHolidayService paidHolidayService) {
    this.userService = userService;
    this.paidHolidayService = paidHolidayService;
  }

  @GetMapping(value = "users/{userId}/paid-holidays")
  public PaidHolidayDto getPaidHolidays(@PathVariable Long userId) {
    User user = userService.findUserById(userId);
    Company company = user.getCompany();
    return paidHolidayService.getPaidHolidays(company.getId());
  }

  @PostMapping(value = "paid-holidays")
  public void createInitialPaidHolidays(@RequestBody String workEmail) {
    User user = userService.findUserByEmail(workEmail);
    Company company = user.getCompany();
    paidHolidayService.createPaidHolidays(company.getId());
  }

  @PatchMapping(value = "paid-holidays")
  public void updateHolidaySelects(@RequestBody List<PaidHolidayPojo> holidaySelectValues) {
    paidHolidayService.updateHolidaySelects(holidaySelectValues);
  }

  @PostMapping(value = "users/{userId}/paid-holiday")
  public void createPaidHoliday(@PathVariable Long userId, @RequestBody PaidHoliday paidHoliday) {
    User user = userService.findUserById(userId);
    Company company = user.getCompany();
    paidHoliday.setCompanyId(company.getId());
    paidHoliday.setIsCustom(true);
    paidHolidayService.createPaidHoliday(paidHoliday);
  }

  @PatchMapping(value = "paid-holidays/{id}")
  public void updatePaidHoliday(@RequestBody PaidHoliday paidHoliday) {
    paidHolidayService.updatePaidHoliday(paidHoliday);
  }

  @DeleteMapping(value = "paid-holidays/{id}")
  public void updatePaidHoliday(@PathVariable Long id) {
    paidHolidayService.deletePaidHoliday(id);
  }
}
