package shamu.company.utils;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import shamu.company.job.entity.JobUserListItem;

public class TupleUtilTests {

  @Test
  void testConvertTo() {
    JobUserListItem jobUserListItem = new JobUserListItem();
    jobUserListItem.setId("1");
    List<JobUserListItem> jobUserListItems = new ArrayList<>();
    jobUserListItems.add(jobUserListItem);
    Assertions.assertDoesNotThrow(
        () ->
            jobUserListItems.stream()
                .map(jobUser -> TupleUtil.convertTo((Tuple) jobUser, JobUserListItem.class)));
  }
}
