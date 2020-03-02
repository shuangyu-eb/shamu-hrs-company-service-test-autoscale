package shamu.company.job.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.job.entity.Job;
import shamu.company.job.repository.JobRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JobServiceTests {

  @Mock
  private JobRepository jobRepository;

  private JobService jobService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    jobService = new JobService(jobRepository);
  }

  @Test
  void findById() {
    String id = "1";
    Job job = new Job();
    Mockito.when(jobRepository.findById(id)).thenReturn(java.util.Optional.of(job));
    Assertions.assertDoesNotThrow(() -> jobService.findById(id));
    Mockito.verify(jobRepository, Mockito.times(1)).findById(Mockito.any());
    Mockito.verify(jobRepository, Mockito.times(1)).findById(Mockito.any());
  }

  @Test()
  void whenJobNotFound_thenThrowResourceNotFoundException() {
    String id = "1";
    Mockito.when(jobRepository.findById(id)).thenThrow(new ResourceNotFoundException(String.format("Job with id %s not found!", id)));
    Assertions.assertThrows(ResourceNotFoundException.class, () -> jobRepository.findById(id));
  }

  @Test
  void findAllByDepartmentId() {
    String id = "1";
    Mockito.when(jobRepository.findAllByDepartmentId(id)).thenReturn(Collections.singletonList(new Job()));
    Assertions.assertDoesNotThrow(() -> jobService.findAllByDepartmentId(id));
  }

  @Test
  void save() {
    Job job = new Job();
    Mockito.when(jobRepository.save(job)).thenReturn(job);
    Assertions.assertDoesNotThrow(() -> jobService.save(job));
    Mockito.verify(jobRepository, Mockito.times(1)).save(Mockito.any());
  }

  @Test
  void deleteInBatch() {
    jobService.deleteInBatch(Collections.singletonList("1"));
    List<String> list = Collections.singletonList("1");
    Mockito.verify(jobRepository, Mockito.times(1)).deleteInBatch(list);
  }

  @Test
  void delete() {
    String id = "1";
    jobService.delete(id);
    Mockito.verify(jobRepository, Mockito.times(1)).delete(id);
  }
}
