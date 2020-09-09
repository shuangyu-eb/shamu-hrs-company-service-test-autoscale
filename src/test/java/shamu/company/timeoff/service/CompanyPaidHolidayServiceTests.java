package shamu.company.timeoff.service;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.timeoff.entity.CompanyPaidHoliday;
import shamu.company.timeoff.repository.CompanyPaidHolidayRepository;

class CompanyPaidHolidayServiceTests {

  private static CompanyPaidHolidayService companyPaidHolidayService;

  @Mock private CompanyPaidHolidayRepository companyPaidHolidayRepository;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    companyPaidHolidayService = new CompanyPaidHolidayService(companyPaidHolidayRepository);
  }

  @Test
  void testFindCompanyPaidHolidayByPaidHolidayIdAndCompanyId() {
    final CompanyPaidHoliday companyPaidHoliday = new CompanyPaidHoliday();

    Mockito.when(
            companyPaidHolidayRepository.findCompanyPaidHolidayByPaidHolidayIdAndCompanyId(
                Mockito.any(), Mockito.any()))
        .thenReturn(companyPaidHoliday);

    Assertions.assertDoesNotThrow(
        () ->
            companyPaidHolidayService.findCompanyPaidHolidayByPaidHolidayIdAndCompanyId("1", "1"));
  }

  @Test
  void testFindAllByCompanyId() {
    final List<CompanyPaidHoliday> companyPaidHolidays = new ArrayList<>();

    Mockito.when(companyPaidHolidayRepository.findAllByCompanyId(Mockito.any()))
        .thenReturn(companyPaidHolidays);

    Assertions.assertDoesNotThrow(() -> companyPaidHolidayService.findAllByCompanyId("1"));
  }
}
