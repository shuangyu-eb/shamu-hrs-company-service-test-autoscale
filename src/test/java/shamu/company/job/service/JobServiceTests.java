package shamu.company.job.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.job.entity.Job;
import shamu.company.job.repository.JobRepository;
import shamu.company.utils.UuidUtil;

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
    assertThatCode(() -> jobService.findById(id)).doesNotThrowAnyException();
    Mockito.verify(jobRepository, Mockito.times(1)).findById(Mockito.any());
    Mockito.verify(jobRepository, Mockito.times(1)).findById(Mockito.any());
  }

  @Test
  void whenJobNotFound_thenThrowResourceNotFoundException() {
    final String id = "1";
    Mockito.when(jobRepository.findById(id))
        .thenThrow(
            new ResourceNotFoundException(
                String.format("Job with id %s not found!", id), id, "job"));
    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> jobRepository.findById(id));
  }

  @Test
  void findAll() {
    Mockito.when(jobRepository.findAll()).thenReturn(Collections.singletonList(new Job()));
    assertThatCode(() -> jobService.findAll()).doesNotThrowAnyException();
  }

  @Test
  void save() {
    final Job job = new Job();
    Mockito.when(jobRepository.save(job)).thenReturn(job);
    assertThatCode(() -> jobService.save(job)).doesNotThrowAnyException();
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

  @Test
  void findByTitleAndCompanyId() {
    final List<Job> jobs = new ArrayList<>();
    final Job job = new Job();
    job.setId(UuidUtil.getUuidString());

    jobs.add(job);

    Mockito.when(jobRepository.findByTitle("123")).thenReturn(jobs);

    assertThat(jobService.findByTitle("123")).isEqualTo(jobs);
  }
}
