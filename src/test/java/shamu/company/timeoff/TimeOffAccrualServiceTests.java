package shamu.company.timeoff;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import shamu.company.timeoff.dto.TimeOffBreakdownItemDto;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency.AccrualFrequencyType;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.pojo.TimeOffBalancePojo;
import shamu.company.timeoff.service.TimeOffAccrualService;
import shamu.company.utils.DateUtil;

class TimeOffAccrualServiceTests {

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

        final LocalDateTime userEnrollTime = LocalDateTime.of(
            LocalDate.of(2016, 1, 1), LocalTime.MAX);

        final String frequencyType =
            TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_ONE.getValue();
        final Boolean result =
            Whitebox.invokeMethod(TimeOffAccrualService.class, "invalidByStartDateAndEndDate",
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

        final LocalDateTime userEnrollTime = LocalDateTime.of(
            LocalDate.of(2016, 1, 1), LocalTime.MAX);

        final String frequencyType =
            TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_ONE.getValue();
        final Boolean result =
            Whitebox.invokeMethod(TimeOffAccrualService.class, "invalidByStartDateAndEndDate",
                Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), userEnrollTime,
                frequencyType);

        Assertions.assertFalse(result);
      }

    }

    @Nested
    class WhenWithFrequencyTypeTwo {

      @Test
      void thenWhenSameAnniversaryYear_thenInvalid() throws Exception {

        final LocalDateTime startDate = LocalDateTime.of(
            LocalDate.of(2016, 8, 2), LocalTime.MAX);

        final LocalDateTime endDate = LocalDateTime.of(
            LocalDate.of(2017, 2, 2), LocalTime.MAX);

        final LocalDateTime userEnrollTime = LocalDateTime.of(
            LocalDate.of(2016, 5, 5), LocalTime.MAX);

        final String frequencyType =
            TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_TWO.getValue();
        final Boolean result =
            Whitebox.invokeMethod(TimeOffAccrualService.class, "invalidByStartDateAndEndDate",
                Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), userEnrollTime,
                frequencyType);

        Assertions.assertTrue(result);
      }

      @Test
      void thenWhenDifferentAnniversaryYear_thenValid() throws Exception {

        final LocalDateTime startDate = LocalDateTime.of(
            LocalDate.of(2016, 8, 2), LocalTime.MAX);

        final LocalDateTime endDate = LocalDateTime.of(
            LocalDate.of(2017, 2, 2), LocalTime.MAX);

        final LocalDateTime userEnrollTime = LocalDateTime.of(
            LocalDate.of(2016, 1, 1), LocalTime.MAX);

        final String frequencyType =
            TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_TWO.getValue();
        final Boolean result =
            Whitebox.invokeMethod(TimeOffAccrualService.class, "invalidByStartDateAndEndDate",
                Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), userEnrollTime,
                frequencyType);

        Assertions.assertFalse(result);
      }
    }

    @Nested
    class WhenWithFrequencyTypeThree {

      @Test
      void thenWhenSameMonth_thenInvalid() throws Exception {

        final LocalDateTime startDate = LocalDateTime.of(
            LocalDate.of(2016, 8, 2), LocalTime.MAX);

        final LocalDateTime endDate = LocalDateTime.of(
            LocalDate.of(2016, 8, 22), LocalTime.MAX);

        final LocalDateTime userEnrollTime = LocalDateTime.of(
            LocalDate.of(2016, 5, 5), LocalTime.MAX);

        final String frequencyType =
            TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_THREE.getValue();
        final Boolean result =
            Whitebox.invokeMethod(TimeOffAccrualService.class, "invalidByStartDateAndEndDate",
                Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), userEnrollTime,
                frequencyType);

        Assertions.assertTrue(result);
      }

      @Test
      void thenWhenDifferentMonth_thenValid() throws Exception {

        final LocalDateTime startDate = LocalDateTime.of(
            LocalDate.of(2016, 3, 2), LocalTime.MAX);

        final LocalDateTime endDate = LocalDateTime.of(
            LocalDate.of(2016, 2, 2), LocalTime.MAX);

        final LocalDateTime userEnrollTime = LocalDateTime.of(
            LocalDate.of(2016, 1, 1), LocalTime.MAX);

        final String frequencyType =
            TimeOffAccrualFrequency.AccrualFrequencyType.FREQUENCY_TYPE_THREE.getValue();
        final Boolean result =
            Whitebox.invokeMethod(TimeOffAccrualService.class, "invalidByStartDateAndEndDate",
                Timestamp.valueOf(startDate), Timestamp.valueOf(endDate), userEnrollTime,
                frequencyType);

        Assertions.assertFalse(result);
      }
    }
  }

  @Nested
  class IsSameAnniversaryYear {

    @Test
    void whenNotInSameAnniversary_thenShouldFail() throws Exception {
      LocalDateTime baseTime = LocalDateTime.now();
      LocalDateTime firstTime = LocalDateTime.now().plusYears(1);
      LocalDateTime secondTime = LocalDateTime.now().plusDays(363);
      final Boolean result =
          Whitebox.invokeMethod(TimeOffAccrualService.class, "isSameAnniversaryYear",
              baseTime, firstTime, secondTime);
      Assertions.assertFalse(result);
    }

    @Test
    void whenInSameAnniversary_thenShouldSuccess() throws Exception {
      LocalDateTime baseTime = LocalDateTime.now();
      LocalDateTime firstTime = LocalDateTime.now().plusDays(364);
      LocalDateTime secondTime = LocalDateTime.now().plusDays(363);
      final Boolean result =
          Whitebox.invokeMethod(TimeOffAccrualService.class, "isSameAnniversaryYear",
              baseTime, firstTime, secondTime);
      Assertions.assertTrue(result);
    }
  }

  @Nested
  class GetScheduleStartBaseTime {

    @Test
    void whenHasDelay_thenReturnDelayedDate() throws Exception {
      LocalDate hireDate = LocalDate.of(2019, 10, 20);
      LocalDate userJoinPolicyDate = LocalDate.of(2019, 10, 21);
      TimeOffPolicyAccrualSchedule accrualSchedule = new TimeOffPolicyAccrualSchedule();
      accrualSchedule.setDaysBeforeAccrualStarts(20);
      TimeOffAccrualFrequency timeOffAccrualFrequency = new TimeOffAccrualFrequency();
      timeOffAccrualFrequency.setId(AccrualFrequencyType.FREQUENCY_TYPE_THREE.getValue());
      accrualSchedule.setTimeOffAccrualFrequency(timeOffAccrualFrequency);

      final LocalDate result =
          Whitebox.invokeMethod(TimeOffAccrualService.class, "getScheduleStartBaseTime",
              hireDate, userJoinPolicyDate, accrualSchedule);
      Assertions.assertTrue(
          result.isEqual(hireDate.plusDays(accrualSchedule.getDaysBeforeAccrualStarts())));
    }

    @Test
    void whenNoDelay_thenReturnUserJoinDate() throws Exception {
      LocalDate hireDate = LocalDate.of(2019, 10, 20);
      LocalDate userJoinPolicyDate = LocalDate.of(2019, 10, 21);
      TimeOffPolicyAccrualSchedule accrualSchedule = new TimeOffPolicyAccrualSchedule();
      TimeOffAccrualFrequency timeOffAccrualFrequency = new TimeOffAccrualFrequency();
      timeOffAccrualFrequency.setId(AccrualFrequencyType.FREQUENCY_TYPE_ONE.getValue());
      accrualSchedule.setTimeOffAccrualFrequency(timeOffAccrualFrequency);

      final LocalDate result =
          Whitebox.invokeMethod(TimeOffAccrualService.class, "getScheduleStartBaseTime",
              hireDate, userJoinPolicyDate, accrualSchedule);
      Assertions.assertTrue(result.isEqual(userJoinPolicyDate));
    }
  }

  @Nested
  class GetValidScheduleOrMilestonePeriod {

    @Test
    void whenBaseTimeIsGreater_thenReturnBaseTime() throws Exception {
      LocalDate baseTime = LocalDate.of(2019, 10, 20);
      LocalDate createTime = LocalDate.of(2019, 10, 19);

      final List<LocalDate> result =
          Whitebox.invokeMethod(TimeOffAccrualService.class, "getValidScheduleOrMilestonePeriod",
              baseTime, DateUtil.fromLocalDate(createTime), null);

      Assertions.assertTrue(result.size() > 0);
      Assertions.assertTrue(result.get(0).isEqual(baseTime));
    }

    @Test
    void whenCreateTimeIsGreater_thenReturnCreateTime() throws Exception {
      LocalDate baseTime = LocalDate.of(2019, 10, 20);
      LocalDateTime baseDateTime = baseTime.atTime(LocalTime.MIN);
      LocalDateTime createDateTime = baseDateTime.plusDays(1);

      ZonedDateTime zonedDateTime = ZonedDateTime.of(createDateTime, ZoneOffset.UTC);
      Timestamp createTime = Timestamp.from(zonedDateTime.toInstant());

      final List<LocalDate> result =
          Whitebox.invokeMethod(TimeOffAccrualService.class, "getValidScheduleOrMilestonePeriod",
              baseTime, createTime, null);

      Assertions.assertTrue(result.size() > 0);
      Assertions.assertTrue(result.get(0).isEqual(DateUtil.fromTimestamp(createTime)));
    }
  }

  @Nested
  class PopulateBreakdownListFromMaxBalance {

    @Test
    void whenNoMaxBalance_thenReturnEmpty() throws Exception {
      List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new ArrayList<>();
      TimeOffBalancePojo timeOffBalancePojo = new TimeOffBalancePojo();
      Whitebox.invokeMethod(TimeOffAccrualService.class, "populateBreakdownListFromMaxBalance",
          resultTimeOffBreakdownItemList, null, timeOffBalancePojo);

      Assertions.assertTrue(resultTimeOffBreakdownItemList.isEmpty());
    }

    @Test
    void whenReachMaxBalance_thenAddBreakdownItem() throws Exception {
      List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new ArrayList<>();
      TimeOffBalancePojo timeOffBalancePojo = new TimeOffBalancePojo();
      timeOffBalancePojo.setBalance(20);
      timeOffBalancePojo.setMaxBalance(19);

      Whitebox.invokeMethod(TimeOffAccrualService.class, "populateBreakdownListFromMaxBalance",
          resultTimeOffBreakdownItemList, LocalDate.now(), timeOffBalancePojo);

      Assertions.assertEquals(1, resultTimeOffBreakdownItemList.size());
    }
  }

  @Nested
  class PopulateBreakdownListFromCarryoverLimit {

    @Test
    void whenNoCarryoverLimit_thenReturnEmpty() throws Exception {
      List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new ArrayList<>();
      TimeOffBalancePojo timeOffBalancePojo = new TimeOffBalancePojo();
      Whitebox.invokeMethod(TimeOffAccrualService.class, "populateBreakdownListFromCarryoverLimit",
          resultTimeOffBreakdownItemList, null, timeOffBalancePojo);

      Assertions.assertTrue(resultTimeOffBreakdownItemList.isEmpty());
    }

    @Test
    void whenHasCarryoverLimit_thenAddBreakdownItem() throws Exception {
      List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new ArrayList<>();
      TimeOffBalancePojo timeOffBalancePojo = new TimeOffBalancePojo();
      timeOffBalancePojo.setBalance(20);
      timeOffBalancePojo.setCarryOverLimit(15);

      Whitebox.invokeMethod(TimeOffAccrualService.class, "populateBreakdownListFromCarryoverLimit",
          resultTimeOffBreakdownItemList, LocalDate.now(), timeOffBalancePojo);

      Assertions.assertEquals(1, resultTimeOffBreakdownItemList.size());
    }
  }

  @Test
  void testPopulateBreakdownListFromAccrualSchedule() throws Exception {
    List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new ArrayList<>();
    TimeOffBalancePojo timeOffBalancePojo = new TimeOffBalancePojo();
    timeOffBalancePojo.setBalance(0);

    Whitebox.invokeMethod(TimeOffAccrualService.class, "populateBreakdownListFromAccrualSchedule",
        resultTimeOffBreakdownItemList, LocalDate.now(), 10, timeOffBalancePojo);

    Assertions.assertEquals(1, resultTimeOffBreakdownItemList.size());
  }

  @Nested
  class PopulateBreakdownAdjustmentBefore {

    @Test
    void whenHasAdjustment_thenShouldAddBreakdownItem() throws Exception {
      List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new LinkedList<>();
      LocalDate untilDate = LocalDate.now();

      TimeOffBalancePojo balancePojo = new TimeOffBalancePojo();
      balancePojo.setBalance(0);

      List<TimeOffBreakdownItemDto> adjustments = new ArrayList<>();
      TimeOffBreakdownItemDto itemDto = new TimeOffBreakdownItemDto();
      itemDto.setDate(untilDate.minusDays(1));
      itemDto.setAmount(20);
      adjustments.add(itemDto);

      Whitebox.invokeMethod(TimeOffAccrualService.class, "populateBreakdownAdjustmentBefore",
          resultTimeOffBreakdownItemList, untilDate, adjustments, balancePojo);

      Assertions.assertEquals(1, resultTimeOffBreakdownItemList.size());
    }

    @Test
    void whenNoAdjustment_thenShouldReturnEmpty() throws Exception {
      List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new LinkedList<>();
      LocalDate untilDate = LocalDate.now();

      TimeOffBalancePojo balancePojo = new TimeOffBalancePojo();
      balancePojo.setBalance(0);

      List<TimeOffBreakdownItemDto> adjustments = new ArrayList<>();
      TimeOffBreakdownItemDto itemDto = new TimeOffBreakdownItemDto();
      itemDto.setDate(untilDate.plusDays(1));
      itemDto.setAmount(20);
      adjustments.add(itemDto);

      Whitebox.invokeMethod(TimeOffAccrualService.class, "populateBreakdownAdjustmentBefore",
          resultTimeOffBreakdownItemList, untilDate, adjustments, balancePojo);

      Assertions.assertTrue(resultTimeOffBreakdownItemList.isEmpty());
    }

  }

  @Test
  void testPopulateRemainingAdjustment() throws Exception {
    List<TimeOffBreakdownItemDto> resultTimeOffBreakdownItemList = new LinkedList<>();
    LocalDate untilDate = LocalDate.now();

    TimeOffBalancePojo balancePojo = new TimeOffBalancePojo();
    balancePojo.setBalance(0);

    List<TimeOffBreakdownItemDto> adjustments = new ArrayList<>();
    TimeOffBreakdownItemDto itemDto = new TimeOffBreakdownItemDto();
    itemDto.setDate(untilDate.plusDays(1));
    itemDto.setAmount(20);
    adjustments.add(itemDto);

    TimeOffBreakdownItemDto itemDtoTwo = new TimeOffBreakdownItemDto();
    itemDtoTwo.setDate(untilDate.plusDays(2));
    itemDtoTwo.setAmount(30);
    adjustments.add(itemDtoTwo);

    Whitebox.invokeMethod(TimeOffAccrualService.class, "populateRemainingAdjustment",
        resultTimeOffBreakdownItemList, adjustments, balancePojo);

    Assertions.assertEquals(2, resultTimeOffBreakdownItemList.size());
  }
}
