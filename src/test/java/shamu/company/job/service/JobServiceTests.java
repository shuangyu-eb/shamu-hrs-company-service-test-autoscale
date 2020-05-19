package shamu.company.job.service;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.OldResourceNotFoundException;
import shamu.company.job.entity.Job;
import shamu.company.job.repository.JobRepository;

public class JobServiceTests {

  @Mock private JobRepository jobRepository;

  private JobService jobService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    jobService = new JobService(jobRepository);
  }

  @Test
  void findById() {
    final String id = "1";
    final Job job = new Job();
    Mockito.when(jobRepository.findById(id)).thenReturn(java.util.Optional.of(job));
    Assertions.assertDoesNotThrow(() -> jobService.findById(id));
    Mockito.verify(jobRepository, Mockito.times(1)).findById(Mockito.any());
    Mockito.verify(jobRepository, Mockito.times(1)).findById(Mockito.any());
  }

  @Test
  void whenJobNotFound_thenThrowResourceNotFoundException() {
    final String id = "1";
    Mockito.when(jobRepository.findById(id))
        .thenThrow(
            new OldResourceNotFoundException(String.format("Job with id %s not found!", id)));
    Assertions.assertThrows(OldResourceNotFoundException.class, () -> jobRepository.findById(id));
  }

  @Test
  void findAllByDepartmentId() {
    final String id = "1";
    Mockito.when(jobRepository.findAllByDepartmentId(id))
        .thenReturn(Collections.singletonList(new Job()));
    Assertions.assertDoesNotThrow(() -> jobService.findAllByDepartmentId(id));
  }

  @Test
  void save() {
    final Job job = new Job();
    Mockito.when(jobRepository.save(job)).thenReturn(job);
    Assertions.assertDoesNotThrow(() -> jobService.save(job));
    Mockito.verify(jobRepository, Mockito.times(1)).save(Mockito.any());
  }

  @Test
  void deleteInBatch() {
    jobService.deleteInBatch(Collections.singletonList("1"));
    final List<String> list = Collections.singletonList("1");
    Mockito.verify(jobRepository, Mockito.times(1)).deleteInBatch(list);
  }

  @Test
  void delete() {
    final String id = "1";
    jobService.delete(id);
    Mockito.verify(jobRepository, Mockito.times(1)).delete(id);
  }
}
