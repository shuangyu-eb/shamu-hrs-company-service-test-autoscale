package shamu.company.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import shamu.company.timeoff.pojo.TimeOffRequestDatesAbstractFragmentPojo;

public abstract class TimeOffRequestDatesAbstractUtil {

  private TimeOffRequestDatesAbstractUtil() {}

  public static String generateTimeOffRequestDatesAbstract(final List<LocalDate> timeOffDates) {
    final List<TimeOffRequestDatesAbstractFragmentPojo> fragments =
        generateTimeOffRequestDatesAbstractFragments(timeOffDates);

    if (fragments.isEmpty()) {
      return "";
    }

    int pointer = 1;
    final StringBuilder timeOffDatesAbstract =
        new StringBuilder()
            .append(
                addMonthBeforeTimeOffRequestDatesAbstract(
                    fragments.get(0).toString(), fragments.get(0)));

    if (fragments.size() == 1) {
      return addYearAfterTimeOffRequestDatesAbstract(
          timeOffDatesAbstract.toString(), fragments.get(0));
    }
    while (pointer < fragments.size()) {
      TimeOffRequestDatesAbstractFragmentPojo pre = fragments.get(pointer - 1);
      TimeOffRequestDatesAbstractFragmentPojo post = fragments.get(pointer);
      LocalDate preEndDate = pre.getLegalEndDate();
      LocalDate postStartDate = post.getStartDate();

      if (preEndDate.getYear() != postStartDate.getYear()) {
        timeOffDatesAbstract
            .append(", ")
            .append(preEndDate.getYear())
            .append(", ")
            .append(post.getStartDateMonth())
            .append(" ")
            .append(post);
      } else if (preEndDate.getMonthValue() != postStartDate.getMonthValue()) {
        timeOffDatesAbstract.append(", ").append(post.getStartDateMonth()).append(" ").append(post);
      } else {
        timeOffDatesAbstract.append(", ").append(post);
      }
      pointer++;
    }
    return addYearAfterTimeOffRequestDatesAbstract(
        timeOffDatesAbstract.toString(), fragments.get(pointer - 1));
  }

  private static List<TimeOffRequestDatesAbstractFragmentPojo>
      generateTimeOffRequestDatesAbstractFragments(final List<LocalDate> dates) {
    if (dates.isEmpty()) {
      return new ArrayList<>();
    }
    int length = dates.size();
    int postPointer = 1;
    int prePointer = 0;
    final List<TimeOffRequestDatesAbstractFragmentPojo> fragments = new ArrayList<>();
    while (prePointer < length) {
      while (postPointer < length) {
        // Search for continuous dates.
        if (ChronoUnit.DAYS.between(dates.get(prePointer), dates.get(postPointer))
            == postPointer - prePointer) {
          postPointer += 1;
          // Search for separated date and generate a fragment.
        } else if (postPointer - prePointer == 1) {
          fragments.add(new TimeOffRequestDatesAbstractFragmentPojo(dates.get(prePointer)));
          prePointer = postPointer++;
          // Collect all continuous dates into a fragment.
        } else {
          fragments.add(
              new TimeOffRequestDatesAbstractFragmentPojo(
                  dates.get(prePointer), dates.get(postPointer - 1)));
          prePointer = postPointer++;
        }
      }
      if (prePointer == length - 1) {
        fragments.add(new TimeOffRequestDatesAbstractFragmentPojo(dates.get(prePointer++)));
      } else {
        fragments.add(
            new TimeOffRequestDatesAbstractFragmentPojo(
                dates.get(prePointer), dates.get(postPointer - 1)));
        prePointer = postPointer;
      }
    }
    return fragments;
  }

  private static String addYearAfterTimeOffRequestDatesAbstract(
      final String timeOffDatesAbstract,
      final TimeOffRequestDatesAbstractFragmentPojo lastFragment) {
    final LocalDate date = lastFragment.getLegalEndDate();
    final LocalDate now = LocalDate.now();
    return now.getYear() == date.getYear()
        ? timeOffDatesAbstract
        : timeOffDatesAbstract + ", " + date.getYear();
  }

  private static String addMonthBeforeTimeOffRequestDatesAbstract(
      final String timeOffDatesAbstract,
      final TimeOffRequestDatesAbstractFragmentPojo firstFragment) {
    return firstFragment.getStartDateMonth() + " " + timeOffDatesAbstract;
  }
}
