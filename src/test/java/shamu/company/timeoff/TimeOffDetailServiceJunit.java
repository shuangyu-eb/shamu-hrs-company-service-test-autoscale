package shamu.company.timeoff;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import shamu.company.timeoff.dto.*;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.service.impl.TimeOffAccrualAnniversaryStrategyServiceImpl;
import shamu.company.timeoff.service.impl.TimeOffAccrualMonthStrategyServiceImpl;
import shamu.company.timeoff.service.impl.TimeOffAccrualNatureStrategyServiceImpl;
import shamu.company.timeoff.service.impl.TimeOffAccrualServiceImpl;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import shamu.company.utils.DateUtil;

class TimeOffDetailServiceJunit {

  @Mock
  private TimeOffAccrualNatureStrategyServiceImpl accrualNatureStrategyService;

  @Mock
  private TimeOffAccrualAnniversaryStrategyServiceImpl accrualAnniversaryStrategyService;

  @Mock
  private TimeOffAccrualMonthStrategyServiceImpl accrualMonthStrategyService;

  @Mock
  private TimeOffAccrualServiceImpl accrualService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class InvalidByStartDateAndEndDate {

    @Nested
    class WhenWithFrequencyTypeOne {

      @Test
      void thenWhenSameStartAndEndYear_thenInvalid() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().withYear(2016);
        startDate = startDate.withMonth(1);

        LocalDateTime endDate = LocalDateTime.now().withYear(2016);
        endDate = endDate.withMonth(7);

        LocalDateTime userEnrollTime = LocalDateTime.of(
            LocalDate.of(2016, 1, 1), LocalTime.MAX);

        Long frequencyType =
            TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_ONE.getValue();
        Boolean result =
            Whitebox.invokeMethod(accrualService, "invalidByStartDateAndEndDate",
            Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), userEnrollTime,
            frequencyType);

        Assertions.assertTrue(result);
      }

      @Test
      void thenWhenDifferentStartAndEndYear_thenValid() throws Exception {

        LocalDateTime startDate = LocalDateTime.now().withYear(2016);
        startDate = startDate.withMonth(1);

        LocalDateTime endDate = LocalDateTime.now().withYear(2017);
        endDate = endDate.withMonth(7);

        LocalDateTime userEnrollTime = LocalDateTime.of(
            LocalDate.of(2016, 1, 1), LocalTime.MAX);

        Long frequencyType =
            TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_ONE.getValue();
        Boolean result =
            Whitebox.invokeMethod(accrualService, "invalidByStartDateAndEndDate",
            Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), userEnrollTime,
            frequencyType);

        Assertions.assertFalse(result);
      }

    }

    @Nested
    class WhenWithFrequencyTypeTwo {

      @Test
      void thenWhenSameAnniversaryYear_thenInvalid() throws Exception {

        LocalDateTime startDate = LocalDateTime.of(
            LocalDate.of(2016, 8, 2), LocalTime.MAX);

        LocalDateTime endDate = LocalDateTime.of(
            LocalDate.of(2017, 2, 2), LocalTime.MAX);

        LocalDateTime userEnrollTime = LocalDateTime.of(
            LocalDate.of(2016, 5, 5), LocalTime.MAX);

        Long frequencyType =
            TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_TWO.getValue();
        Boolean result =
            Whitebox.invokeMethod(accrualService, "invalidByStartDateAndEndDate",
            Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), userEnrollTime,
            frequencyType);

        Assertions.assertTrue(result);
      }

      @Test
      void thenWhenDifferentAnniversaryYear_thenValid() throws Exception {

        LocalDateTime startDate = LocalDateTime.of(
            LocalDate.of(2016, 8, 2), LocalTime.MAX);

        LocalDateTime endDate = LocalDateTime.of(
            LocalDate.of(2017, 2, 2), LocalTime.MAX);

        LocalDateTime userEnrollTime = LocalDateTime.of(
            LocalDate.of(2016, 1, 1), LocalTime.MAX);

        Long frequencyType =
            TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_TWO.getValue();
        Boolean result =
            Whitebox.invokeMethod(accrualService, "invalidByStartDateAndEndDate",
            Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), userEnrollTime,
            frequencyType);

        Assertions.assertFalse(result);
      }
    }

    @Nested
    class WhenWithFrequencyTypeThree {

      @Test
      void thenWhenSameMonth_thenInvalid() throws Exception {

        LocalDateTime startDate = LocalDateTime.of(
            LocalDate.of(2016, 8, 2), LocalTime.MAX);

        LocalDateTime endDate = LocalDateTime.of(
            LocalDate.of(2016, 8, 22), LocalTime.MAX);

        LocalDateTime userEnrollTime = LocalDateTime.of(
            LocalDate.of(2016, 5, 5), LocalTime.MAX);

        Long frequencyType =
            TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_THREE.getValue();
        Boolean result =
            Whitebox.invokeMethod(accrualService, "invalidByStartDateAndEndDate",
            Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), userEnrollTime,
            frequencyType);

        Assertions.assertTrue(result);
      }

      @Test
      void thenWhenDifferentMonth_thenValid() throws Exception {

        LocalDateTime startDate = LocalDateTime.of(
            LocalDate.of(2016, 3, 2), LocalTime.MAX);

        LocalDateTime endDate = LocalDateTime.of(
            LocalDate.of(2016, 2, 2), LocalTime.MAX);

        LocalDateTime userEnrollTime = LocalDateTime.of(
            LocalDate.of(2016, 1, 1), LocalTime.MAX);

        Long frequencyType =
            TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_THREE.getValue();
        Boolean result =
            Whitebox.invokeMethod(accrualService, "invalidByStartDateAndEndDate",
            Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), userEnrollTime,
            frequencyType);

        Assertions.assertFalse(result);
      }
    }
  }

  @Nested
  class GetValidYearPeriod {

    @Test
    void whenSameYear_thenReturnEmpty() throws Exception {

      Timestamp startDate = Timestamp.valueOf(
          LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.MAX));
      Timestamp endDate = Timestamp.valueOf(
          LocalDateTime.of(LocalDate.of(2016, 2, 2), LocalTime.MAX));

      List<Integer> validYears = Whitebox.invokeMethod(
          accrualNatureStrategyService, "getValidYearPeriod", startDate, endDate);

      Assertions.assertTrue( validYears.isEmpty());
    }
  }

  @Test
  void whenDifferentYear_thenReturnFilledArray() throws Exception {

    Timestamp startDate = Timestamp.valueOf(
        LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.MAX));
    Timestamp endDate = Timestamp.valueOf(
        LocalDateTime.of(LocalDate.of(2017, 2, 2), LocalTime.MAX));

    List<Integer> validYears =
        Whitebox.invokeMethod(accrualNatureStrategyService, "getValidYearPeriod",
            startDate, endDate);

    Assertions.assertAll(
        () -> Assertions.assertEquals(1, validYears.size()),
        () -> Assertions.assertEquals(validYears.get(0).intValue(), 2016));
  }

  @Nested
  class GetValidAnniversaryPeriod {

    @Test
    void whenSameAnniversary_thenReturnEmpty() throws Exception {

      Timestamp startDate = Timestamp.valueOf(
          LocalDateTime.of(LocalDate.of(2016, 6, 1), LocalTime.MAX));
      Timestamp endDate = Timestamp.valueOf(
          LocalDateTime.of(LocalDate.of(2017, 3, 2), LocalTime.MAX));

      Timestamp userEnrollTime = Timestamp.valueOf(
          LocalDateTime.of(LocalDate.of(2016, 4, 4), LocalTime.MAX));

      List<Integer> validYears =
          Whitebox.invokeMethod(accrualAnniversaryStrategyService,
              "getValidAnniversaryPeriod", userEnrollTime, startDate, endDate);

      Assertions.assertTrue( validYears.isEmpty());
    }

    @Test
    void whenDifferentAnniversary_thenReturnFilledArray() throws Exception {

      Timestamp startDate = Timestamp.valueOf(
          LocalDateTime.of(LocalDate.of(2016, 2, 1), LocalTime.MAX));
      Timestamp endDate = Timestamp.valueOf(
          LocalDateTime.of(LocalDate.of(2017, 4, 1), LocalTime.MAX));

      Timestamp userEnrollTime = Timestamp.valueOf(
          LocalDateTime.of(LocalDate.of(2016, 3, 4), LocalTime.MAX));

      List<LocalDateTime> validYears =
          Whitebox.invokeMethod(accrualAnniversaryStrategyService,
              "getValidAnniversaryPeriod", userEnrollTime, startDate, endDate);

      Assertions.assertAll(
          () -> Assertions.assertEquals(1, validYears.size()),
          () -> Assertions.assertTrue(
              validYears.get(0).isEqual(DateUtil.toLocalDateTime(userEnrollTime))));
    }
  }

  @Nested
  class GetValidMonthPeriod {

    @Test
    void whenAfterEndDate_thenReturnEmpty() throws Exception {

      Timestamp startDate = Timestamp.valueOf(
          LocalDateTime.of(LocalDate.of(2016, 6, 1), LocalTime.MAX));
      Timestamp endDate = Timestamp.valueOf(
          LocalDateTime.of(LocalDate.of(2016, 5, 2), LocalTime.MAX));

      List<Integer> monthPeriods =
          Whitebox.invokeMethod(accrualMonthStrategyService, "getValidMonthPeriod",
              startDate, endDate);

      Assertions.assertTrue( monthPeriods.isEmpty());
    }

    @Test
    void whenBeforeEndDate_thenReturnFilledArray() throws Exception {

      Timestamp startDate = Timestamp.valueOf(
          LocalDateTime.of(LocalDate.of(2016, 6, 2), LocalTime.MAX));
      Timestamp endDate = Timestamp.valueOf(
          LocalDateTime.of(LocalDate.of(2016, 7, 1), LocalTime.MAX));

      List<LocalDateTime> monthPeriods =
          Whitebox.invokeMethod(accrualMonthStrategyService, "getValidMonthPeriod",
              startDate, endDate);

      Assertions.assertAll(
          () -> Assertions.assertEquals(1, monthPeriods.size()),
          () -> Assertions.assertTrue(
              monthPeriods.get(0).isEqual(DateUtil.toLocalDateTime(startDate))));
    }
  }

  @Nested
  class AddMissingYearDto {

    @Test
    void whenMissingOneYear_thenReturnFilled () throws Exception {
      List<TimeOffBreakdownYearDto> timeOffBreakdownYearDtos = new ArrayList<>();
      TimeOffBreakdownYearDto startBreakdownYearDto = new TimeOffBreakdownYearDto();
      startBreakdownYearDto.setYear(2016);
      timeOffBreakdownYearDtos.add(startBreakdownYearDto);

      TimeOffBreakdownYearDto endBreakdownYearDto = new TimeOffBreakdownYearDto();
      endBreakdownYearDto.setYear(2018);
      timeOffBreakdownYearDtos.add(endBreakdownYearDto);

      LinkedList<TimeOffBreakdownYearDto> filledYearDtos =
          Whitebox.invokeMethod(accrualNatureStrategyService, "addMissingYearDto",
              timeOffBreakdownYearDtos);

      Assertions.assertEquals(filledYearDtos.size(), 3);
    }
  }

  @Nested
  class AddMissingAnniversaryYearDto {

    @Test
    void whenMissingOneAnniversaryYear_thenReturnFilled () throws Exception {
      List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownYearDtos = new ArrayList<>();
      TimeOffBreakdownAnniversaryDto startBreakdownDto = new TimeOffBreakdownAnniversaryDto();
      startBreakdownDto.setDate(
          LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.MIN));
      timeOffBreakdownYearDtos.add(startBreakdownDto);

      TimeOffBreakdownAnniversaryDto endBreakdownYearDto = new TimeOffBreakdownAnniversaryDto();
      endBreakdownYearDto.setDate(
          LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.MIN));
      timeOffBreakdownYearDtos.add(endBreakdownYearDto);

      LinkedList<TimeOffBreakdownYearDto> filledYearDtos =
          Whitebox.invokeMethod(accrualAnniversaryStrategyService,
              "addMissingAnniversaryYearDto", timeOffBreakdownYearDtos);

      Assertions.assertEquals(filledYearDtos.size(), 3);
    }
  }

  @Nested
  class AddMissingMonthDto {

    @Test
    void whenMissingOneMonth_thenReturnFilled () throws Exception {
      List<TimeOffBreakdownMonthDto> timeOffBreakdownYearDtos = new ArrayList<>();
      TimeOffBreakdownMonthDto startBreakdownDto = new TimeOffBreakdownMonthDto();
      startBreakdownDto.setDate(
          LocalDateTime.of(LocalDate.of(2016, 1, 1), LocalTime.MIN));
      timeOffBreakdownYearDtos.add(startBreakdownDto);

      TimeOffBreakdownMonthDto endBreakdownYearDto = new TimeOffBreakdownMonthDto();
      endBreakdownYearDto.setDate(
          LocalDateTime.of(LocalDate.of(2016, 3, 1), LocalTime.MIN));
      timeOffBreakdownYearDtos.add(endBreakdownYearDto);

      LinkedList<TimeOffBreakdownYearDto> filledYearDtos =
          Whitebox.invokeMethod(accrualMonthStrategyService, "addMissingMonthDto",
              timeOffBreakdownYearDtos);

      Assertions.assertEquals(filledYearDtos.size(), 3);
    }
  }


  @Nested
  class GetFinalTimeOffBreakdown {

    TimeOffBreakdownItemDto startingBreakdown;

    List<TimeOffBreakdownItemDto> balanceAdjustmentList = new ArrayList<>();

    @BeforeEach
    void setUp() {
      startingBreakdown = new TimeOffBreakdownItemDto();
      startingBreakdown.setDate(
          LocalDateTime.of(LocalDate.of(2016, 3, 10), LocalTime.MIN));
      startingBreakdown.setAmount(10);
      startingBreakdown.setBalance(10);

      TimeOffBreakdownItemDto timeOffBreakdownItemDto = new TimeOffBreakdownItemDto();
      timeOffBreakdownItemDto.setDate(
          LocalDateTime.of(LocalDate.of(2016, 5, 5), LocalTime.MIN));
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
        TimeOffBreakdownYearDto firstBreakdownYearDto = new TimeOffBreakdownYearDto();
        firstBreakdownYearDto.setAccrualHours(10);
        firstBreakdownYearDto.setYear(2016);
        firstBreakdownYearDto.setCarryoverLimit(3);
        firstBreakdownYearDto.setMaxBalance(20);
        timeOffBreakdownYearDtoList.add(firstBreakdownYearDto);

        TimeOffBreakdownYearDto secondBreakdownYearDto = new TimeOffBreakdownYearDto();
        secondBreakdownYearDto.setAccrualHours(6);
        secondBreakdownYearDto.setYear(2017);
        secondBreakdownYearDto.setCarryoverLimit(3);
        secondBreakdownYearDto.setMaxBalance(20);
        timeOffBreakdownYearDtoList.add(secondBreakdownYearDto);
      }

      @Test
      void testGetFinalTimeOffBreakdown () throws Exception {
        TimeOffBreakdownDto timeOffBreakdownDto =
            Whitebox.invokeMethod(accrualNatureStrategyService,
                "getFinalTimeOffBreakdown", timeOffBreakdownYearDtoList,
                startingBreakdown, balanceAdjustmentList);
        Assertions.assertEquals(timeOffBreakdownDto.getBalance().intValue(), 19);
      }
    }

    @Nested
    class GetFinalByAnniversaryYear {

      List<TimeOffBreakdownAnniversaryDto> timeOffBreakdownAnniversaryDtoList = new ArrayList<>();

      @BeforeEach
      void setUp() {
        TimeOffBreakdownAnniversaryDto firstBreakdownYearDto = new TimeOffBreakdownAnniversaryDto();
        LocalDateTime firstYearDate =
            LocalDateTime.of(LocalDate.of(2016, 5, 2), LocalTime.MIN);
        firstBreakdownYearDto.setDate(firstYearDate);
        firstBreakdownYearDto.setAccrualHours(10);
        firstBreakdownYearDto.setMaxBalance(7);
        firstBreakdownYearDto.setCarryoverLimit(2);
        timeOffBreakdownAnniversaryDtoList.add(firstBreakdownYearDto);

        TimeOffBreakdownAnniversaryDto secondBreakdownYearDto = new TimeOffBreakdownAnniversaryDto();
        LocalDateTime secondYearDate =
            LocalDateTime.of(LocalDate.of(2017, 5, 2), LocalTime.MIN);
        secondBreakdownYearDto.setAccrualHours(10);
        secondBreakdownYearDto.setDate(secondYearDate);
        secondBreakdownYearDto.setCarryoverLimit(5);
        secondBreakdownYearDto.setMaxBalance(16);
        timeOffBreakdownAnniversaryDtoList.add(secondBreakdownYearDto);
      }

      @Test
      void testGetFinalAnniversaryBreakdown () throws Exception {
        TimeOffBreakdownDto timeOffBreakdownDto =
            Whitebox.invokeMethod(accrualAnniversaryStrategyService,
                "getFinalAnniversaryBreakdown", timeOffBreakdownAnniversaryDtoList,
                startingBreakdown, balanceAdjustmentList);
        Assertions.assertEquals(13, timeOffBreakdownDto.getBalance().intValue());
      }
    }


    @Nested
    class GetFinalMonthBreakdown {

      List<TimeOffBreakdownMonthDto> timeOffBreakdownMonthDtos = new ArrayList<>();

      @BeforeEach
      void setUp() {
        TimeOffBreakdownMonthDto firstBreakdownMonthDto = new TimeOffBreakdownMonthDto();
        LocalDateTime firstMonthDate =
            LocalDateTime.of(LocalDate.of(2016, 5, 1), LocalTime.MIN);
        firstBreakdownMonthDto.setDate(firstMonthDate);
        firstBreakdownMonthDto.setAccrualHours(10);
        firstBreakdownMonthDto.setLastMonthOfTheYear(false);
        timeOffBreakdownMonthDtos.add(firstBreakdownMonthDto);

        TimeOffBreakdownMonthDto secondBreakdownMonthDto = new TimeOffBreakdownMonthDto();
        LocalDateTime secondMonthDate =
        LocalDateTime.of(LocalDate.of(2016, 6, 1), LocalTime.MIN);
        secondBreakdownMonthDto.setAccrualHours(10);
        secondBreakdownMonthDto.setDate(secondMonthDate);
        secondBreakdownMonthDto.setLastMonthOfTheYear(false);
        timeOffBreakdownMonthDtos.add(secondBreakdownMonthDto);
      }

      @Test
      void testGetFinalMonthBreakdown () throws Exception {
        TimeOffBreakdownDto timeOffBreakdownDto =
            Whitebox.invokeMethod(accrualMonthStrategyService,
                "getFinalMonthBreakdown", timeOffBreakdownMonthDtos,
                startingBreakdown, balanceAdjustmentList);
        Assertions.assertEquals(26, timeOffBreakdownDto.getBalance().intValue());
      }
    }
  }
}
