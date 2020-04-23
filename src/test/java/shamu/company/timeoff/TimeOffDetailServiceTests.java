package shamu.company.timeoff;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import shamu.company.timeoff.dto.TimeOffBreakdownAnniversaryDto;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.dto.TimeOffBreakdownMonthDto;
import shamu.company.timeoff.dto.TimeOffBreakdownYearDto;
import shamu.company.timeoff.service.TimeOffAccrualAnniversaryStrategyService;
import shamu.company.timeoff.service.TimeOffAccrualMonthStrategyService;
import shamu.company.timeoff.service.TimeOffAccrualNatureStrategyService;

class TimeOffDetailServiceTests {

  @Mock private TimeOffAccrualNatureStrategyService accrualNatureStrategyService;

  @Mock private TimeOffAccrualAnniversaryStrategyService accrualAnniversaryStrategyService;

  @Mock private TimeOffAccrualMonthStrategyService accrualMonthStrategyService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void whenDifferentYear_thenReturnMoreElements() throws Exception {

    final LocalDate startDate = LocalDate.of(2016, 1, 1);
    final LocalDate endDate = LocalDate.of(2017, 2, 2);

    final List<Integer> validYears =
        Whitebox.invokeMethod(
            accrualNatureStrategyService,
            "getValidYearPeriod",
            startDate,
            endDate,
            LocalDate.now());

    Assertions.assertEquals(2, validYears.size());
  }

  @Nested
  class GetValidYearPeriod {

    @Test
    void whenSameYear_thenReturnOneElement() throws Exception {

      final LocalDate startDate = LocalDate.of(2016, 1, 1);
      final LocalDate endDate = LocalDate.of(2016, 2, 2);

      final List<Integer> validYears =
          Whitebox.invokeMethod(
              accrualNatureStrategyService,
              "getValidYearPeriod",
              startDate,
              endDate,
              LocalDate.now());

      Assertions.assertEquals(1, validYears.size());
    }
  }

  @Nested
  class GetValidAnniversaryPeriod {

    @Test
    void whenSameAnniversary_thenReturnEmpty() throws Exception {

      final LocalDate startDate = LocalDate.of(2016, 6, 1);
      final LocalDate endDate = LocalDate.of(2017, 3, 2);

      final LocalDate userJoinDate = LocalDate.of(2016, 4, 4);

      final List<LocalDate> validYears =
          Whitebox.invokeMethod(
              accrualAnniversaryStrategyService,
              "getValidAnniversaryPeriod",
              userJoinDate,
              startDate,
              endDate,
              LocalDate.now());

      Assertions.assertTrue(validYears.isEmpty());
    }

    @Test
    void whenDifferentAnniversary_thenReturnFilledArray() throws Exception {

      final LocalDate startDate = LocalDate.of(2016, 2, 1);
      final LocalDate endDate = LocalDate.of(2017, 4, 1);

      final LocalDate userJoinDate = LocalDate.of(2016, 3, 4);

      final List<LocalDate> validYears =
          Whitebox.invokeMethod(
              accrualAnniversaryStrategyService,
              "getValidAnniversaryPeriod",
              userJoinDate,
              startDate,
              endDate,
              LocalDate.now());

      Assertions.assertAll(
          () -> Assertions.assertEquals(1, validYears.size()),
          () -> Assertions.assertTrue(validYears.get(0).isEqual(userJoinDate)));
    }
  }

  @Nested
  class GetValidMonthPeriod {

    @Test
    void whenAfterEndDate_thenReturnEmpty() throws Exception {

      final LocalDate startDate = LocalDate.of(2016, 6, 1);
      final LocalDate endDate = LocalDate.of(2016, 5, 2);

      final List<LocalDate> monthPeriods =
          Whitebox.invokeMethod(
              accrualMonthStrategyService,
              "getValidMonthPeriod",
              startDate,
              endDate,
              LocalDate.now());

      Assertions.assertTrue(monthPeriods.isEmpty());
    }

    @Test
    void whenBeforeEndDate_thenReturnFilledArray() throws Exception {

      final LocalDate startDate = LocalDate.of(2016, 6, 2);
      final LocalDate endDate = LocalDate.of(2016, 7, 1);

      final List<LocalDate> monthPeriods =
          Whitebox.invokeMethod(
              accrualMonthStrategyService,
              "getValidMonthPeriod",
              startDate,
              endDate,
              LocalDate.now());

      Assertions.assertEquals(2, monthPeriods.size());
    }
  }

  @Nested
  class AddMissingYearDto {

    @Test
    void whenMissingOneYear_thenReturnFilled() throws Exception {
      final List<TimeOffBreakdownYearDto> timeOffBreakdownYearDtos = new ArrayList<>();
      final TimeOffBreakdownYearDto startBreakdownYearDto = new TimeOffBreakdownYearDto();
      startBreakdownYearDto.setDate(LocalDate.MIN.withYear(2016));
      timeOffBreakdownYearDtos.add(startBreakdownYearDto);

      final TimeOffBreakdownYearDto endBreakdownYearDto = new TimeOffBreakdownYearDto();
      endBreakdownYearDto.setDate(LocalDate.MIN.withYear(2018));
      timeOffBreakdownYearDtos.add(endBreakdownYearDto);

      final LinkedList<TimeOffBreakdownYearDto> filledYearDtos =
          Whitebox.invokeMethod(
              accrualNatureStrategyService, "addMissingYearDto", timeOffBreakdownYearDtos);

      Assertions.assertEquals(filledYearDtos.size(), 3);
    }
  }

  @Nested
  class AddMissingAnniversaryYearDto {

    @Test
    void whenMissingOneAnniversaryYear_thenReturnFilled() throws Exception {
      final List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownYearDtos = new ArrayList<>();
      final TimeOffBreakdownAnniversaryDto startBreakdownDto = new TimeOffBreakdownAnniversaryDto();
      startBreakdownDto.setDate(LocalDate.of(2016, 1, 1));
      timeOffBreakdownYearDtos.add(startBreakdownDto);

      final TimeOffBreakdownAnniversaryDto endBreakdownYearDto =
          new TimeOffBreakdownAnniversaryDto();
      endBreakdownYearDto.setDate(LocalDate.of(2018, 1, 1));
      timeOffBreakdownYearDtos.add(endBreakdownYearDto);

      final LinkedList<TimeOffBreakdownYearDto> filledYearDtos =
          Whitebox.invokeMethod(
              accrualAnniversaryStrategyService,
              "addMissingAnniversaryYearDto",
              timeOffBreakdownYearDtos);

      Assertions.assertEquals(filledYearDtos.size(), 3);
    }
  }

  @Nested
  class AddMissingMonthDto {

    @Test
    void whenMissingOneMonth_thenReturnFilled() throws Exception {
      final List<TimeOffBreakdownMonthDto> timeOffBreakdownYearDtos = new ArrayList<>();
      final TimeOffBreakdownMonthDto startBreakdownDto = new TimeOffBreakdownMonthDto();
      startBreakdownDto.setDate(LocalDate.of(2016, 1, 1));
      timeOffBreakdownYearDtos.add(startBreakdownDto);

      final TimeOffBreakdownMonthDto endBreakdownYearDto = new TimeOffBreakdownMonthDto();
      endBreakdownYearDto.setDate(LocalDate.of(2016, 3, 1));
      timeOffBreakdownYearDtos.add(endBreakdownYearDto);

      final LinkedList<TimeOffBreakdownYearDto> filledYearDtos =
          Whitebox.invokeMethod(
              accrualMonthStrategyService, "addMissingMonthDto", timeOffBreakdownYearDtos);

      Assertions.assertEquals(filledYearDtos.size(), 3);
    }
  }

  @Nested
  class GetFinalTimeOffBreakdown {

    TimeOffBreakdownYearDto startingBreakdown;

    List<TimeOffBreakdownItemDto> balanceAdjustmentList = new ArrayList<>();

    @BeforeEach
    void setUp() {
      startingBreakdown = new TimeOffBreakdownYearDto();
      startingBreakdown.setDate(LocalDate.of(2016, 3, 10));
      startingBreakdown.setAccrualHours(10);

      final TimeOffBreakdownItemDto timeOffBreakdownItemDto = new TimeOffBreakdownItemDto();
      timeOffBreakdownItemDto.setDate(LocalDate.of(2016, 5, 5));
      timeOffBreakdownItemDto.setAmount(-4);
      timeOffBreakdownItemDto.setDetail("Request time off");
      balanceAdjustmentList.add(timeOffBreakdownItemDto);
    }

    @Nested
    class getFinalByYear {

      List<TimeOffBreakdownYearDto> timeOffBreakdownYearDtoList;

      @BeforeEach
      void setUp() {
        timeOffBreakdownYearDtoList = new ArrayList<>();
        timeOffBreakdownYearDtoList.add(startingBreakdown);
        final TimeOffBreakdownYearDto firstBreakdownYearDto = new TimeOffBreakdownYearDto();
        firstBreakdownYearDto.setAccrualHours(10);
        firstBreakdownYearDto.setDate(LocalDate.MIN.withYear(2016));
        firstBreakdownYearDto.setCarryoverLimit(3);
        firstBreakdownYearDto.setMaxBalance(20);
        timeOffBreakdownYearDtoList.add(firstBreakdownYearDto);

        final TimeOffBreakdownYearDto secondBreakdownYearDto = new TimeOffBreakdownYearDto();
        secondBreakdownYearDto.setAccrualHours(6);
        secondBreakdownYearDto.setDate(LocalDate.MIN.withYear(2017));
        secondBreakdownYearDto.setCarryoverLimit(3);
        secondBreakdownYearDto.setMaxBalance(20);
        timeOffBreakdownYearDtoList.add(secondBreakdownYearDto);
      }

      @Test
      void testGetFinalTimeOffBreakdown() throws Exception {
        final TimeOffBreakdownDto timeOffBreakdownDto =
            Whitebox.invokeMethod(
                accrualNatureStrategyService,
                "getFinalTimeOffBreakdown",
                timeOffBreakdownYearDtoList,
                balanceAdjustmentList);
        Assertions.assertEquals(9, timeOffBreakdownDto.getBalance().intValue());
      }
    }

    @Nested
    class GetFinalByAnniversaryYear {

      List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownAnniversaryDtoList = new ArrayList<>();

      @BeforeEach
      void setUp() {
        final TimeOffBreakdownAnniversaryDto firstBreakdownYearDto =
            new TimeOffBreakdownAnniversaryDto();
        final LocalDate firstYearDate = LocalDate.of(2016, 5, 2);
        firstBreakdownYearDto.setDate(firstYearDate);
        firstBreakdownYearDto.setAccrualHours(10);
        firstBreakdownYearDto.setMaxBalance(7);
        firstBreakdownYearDto.setCarryoverLimit(2);
        timeOffBreakdownAnniversaryDtoList.add(firstBreakdownYearDto);

        final TimeOffBreakdownAnniversaryDto secondBreakdownYearDto =
            new TimeOffBreakdownAnniversaryDto();
        final LocalDate secondYearDate = LocalDate.of(2017, 5, 2);
        secondBreakdownYearDto.setAccrualHours(10);
        secondBreakdownYearDto.setDate(secondYearDate);
        secondBreakdownYearDto.setCarryoverLimit(5);
        secondBreakdownYearDto.setMaxBalance(16);
        timeOffBreakdownAnniversaryDtoList.add(secondBreakdownYearDto);
      }

      @Test
      void testGetFinalAnniversaryBreakdown() throws Exception {
        final TimeOffBreakdownDto timeOffBreakdownDto =
            Whitebox.invokeMethod(
                accrualAnniversaryStrategyService,
                "getFinalAnniversaryBreakdown",
                timeOffBreakdownAnniversaryDtoList,
                balanceAdjustmentList);
        Assertions.assertEquals(12, timeOffBreakdownDto.getBalance().intValue());
      }
    }

    @Nested
    class GetFinalMonthBreakdown {

      List<TimeOffBreakdownMonthDto> timeOffBreakdownMonthDtos = new ArrayList<>();

      @BeforeEach
      void setUp() {
        final TimeOffBreakdownMonthDto firstBreakdownMonthDto = new TimeOffBreakdownMonthDto();
        final LocalDate firstMonthDate = LocalDate.of(2016, 5, 1);
        firstBreakdownMonthDto.setDate(firstMonthDate);
        firstBreakdownMonthDto.setAccrualHours(10);
        firstBreakdownMonthDto.setLastMonthOfPreviousAnniversaryYear(false);
        timeOffBreakdownMonthDtos.add(firstBreakdownMonthDto);

        final TimeOffBreakdownMonthDto secondBreakdownMonthDto = new TimeOffBreakdownMonthDto();
        final LocalDate secondMonthDate = LocalDate.of(2016, 6, 1);
        secondBreakdownMonthDto.setAccrualHours(10);
        secondBreakdownMonthDto.setDate(secondMonthDate);
        secondBreakdownMonthDto.setLastMonthOfPreviousAnniversaryYear(false);
        timeOffBreakdownMonthDtos.add(secondBreakdownMonthDto);
      }

      @Test
      void testGetFinalMonthBreakdown() throws Exception {
        final TimeOffBreakdownDto timeOffBreakdownDto =
            Whitebox.invokeMethod(
                accrualMonthStrategyService,
                "getFinalMonthBreakdown",
                timeOffBreakdownMonthDtos,
                balanceAdjustmentList);
        Assertions.assertEquals(16, timeOffBreakdownDto.getBalance().intValue());
      }
    }
  }
}
