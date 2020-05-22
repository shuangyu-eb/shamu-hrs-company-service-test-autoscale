package shamu.company.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import shamu.company.timeoff.pojo.TimeOffRequestDatesPreviewFragmentPojo;

public abstract class TimeOffRequestDatesPreviewUtils {

  private TimeOffRequestDatesPreviewUtils() {}

  public static String generateTimeOffRequestDatesPreview(final List<LocalDate> timeOffDates) {
    final List<TimeOffRequestDatesPreviewFragmentPojo> fragments =
        generateTimeOffRequestDatesPreviewFragments(timeOffDates);

    if (fragments.isEmpty()) {
      return "";
    }

    int pointer = 1;
    final StringBuilder timeOffDatesPreview =
        new StringBuilder()
            .append(
                addMonthBeforeTimeOffRequestDatesPreview(
                    fragments.get(0).toString(), fragments.get(0)));

    if (fragments.size() == 1) {
      return addYearAfterTimeOffRequestDatesPreview(
          timeOffDatesPreview.toString(), fragments.get(0));
    }
    while (pointer < fragments.size()) {
      TimeOffRequestDatesPreviewFragmentPojo pre = fragments.get(pointer - 1);
      TimeOffRequestDatesPreviewFragmentPojo post = fragments.get(pointer);
      LocalDate preEndDate = pre.getLegalEndDate();
      LocalDate postStartDate = post.getStartDate();

      if (preEndDate.getYear() != postStartDate.getYear()) {
        timeOffDatesPreview
            .append(", ")
            .append(preEndDate.getYear())
            .append(", ")
            .append(post.getStartDateMonth())
            .append(" ")
            .append(post);
      } else if (preEndDate.getMonthValue() != postStartDate.getMonthValue()) {
        timeOffDatesPreview.append(", ").append(post.getStartDateMonth()).append(" ").append(post);
      } else {
        timeOffDatesPreview.append(", ").append(post);
      }
      pointer++;
    }
    return addYearAfterTimeOffRequestDatesPreview(
        timeOffDatesPreview.toString(), fragments.get(pointer - 1));
  }

  private static List<TimeOffRequestDatesPreviewFragmentPojo>
      generateTimeOffRequestDatesPreviewFragments(final List<LocalDate> dates) {
    if (dates.isEmpty()) {
      return new ArrayList<>();
    }
    int length = dates.size();
    int postPointer = 1;
    int prePointer = 0;
    final List<TimeOffRequestDatesPreviewFragmentPojo> fragments = new ArrayList<>();
    while (postPointer < length) {
      // Search for continuous dates.
      if (ChronoUnit.DAYS.between(dates.get(prePointer), dates.get(postPointer))
          == postPointer - prePointer) {
        postPointer += 1;
        // Search for separated date and generate a fragment.
      } else if (postPointer - prePointer == 1) {
        fragments.add(new TimeOffRequestDatesPreviewFragmentPojo(dates.get(prePointer)));
        prePointer = postPointer++;
        // Collect all continuous dates into a fragment.
      } else {
        fragments.add(
            new TimeOffRequestDatesPreviewFragmentPojo(
                dates.get(prePointer), dates.get(postPointer - 1)));
        prePointer = postPointer++;
      }
    }
    if (prePointer == length - 1) {
      fragments.add(new TimeOffRequestDatesPreviewFragmentPojo(dates.get(prePointer)));
    } else {
      fragments.add(
          new TimeOffRequestDatesPreviewFragmentPojo(
              dates.get(prePointer), dates.get(postPointer - 1)));
    }
    return fragments;
  }

  private static String addYearAfterTimeOffRequestDatesPreview(
      final String timeOffDatesPreview, final TimeOffRequestDatesPreviewFragmentPojo lastFragment) {
    final LocalDate date = lastFragment.getLegalEndDate();
    final LocalDate now = LocalDate.now();
    return now.getYear() == date.getYear()
        ? timeOffDatesPreview
        : String.format("%s, %d", timeOffDatesPreview, date.getYear());
  }

  private static String addMonthBeforeTimeOffRequestDatesPreview(
      final String timeOffDatesPreview,
      final TimeOffRequestDatesPreviewFragmentPojo firstFragment) {
    return String.format("%s %s", firstFragment.getStartDateMonth(), timeOffDatesPreview);
  }
}
