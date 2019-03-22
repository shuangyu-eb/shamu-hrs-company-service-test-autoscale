package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.Job;
import com.tardisone.companyservice.repository.JobRepository;
import com.tardisone.companyservice.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    JobRepository jobRepository;

    @Override
    public Job findJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }


}
