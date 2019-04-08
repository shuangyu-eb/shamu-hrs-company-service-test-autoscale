package com.tardisone.companyservice.service.serviceImpl;

import com.tardisone.companyservice.entity.Job;
import com.tardisone.companyservice.entity.JobUser;
import com.tardisone.companyservice.repository.DepartmentRepository;
import com.tardisone.companyservice.repository.JobRepository;
import com.tardisone.companyservice.service.InformationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class InformationServiceImpl implements InformationService {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Override
    public Map<String, String> getAllJobInformation() {
        List<Job> jobList = jobRepository.findAll();
        for(Job job : jobList){
            long jobId = job.getId();
        }
        return null;
    }

    @Override
    public Map<String, String> getAllEmploymentType() {
        return null;
    }
}
