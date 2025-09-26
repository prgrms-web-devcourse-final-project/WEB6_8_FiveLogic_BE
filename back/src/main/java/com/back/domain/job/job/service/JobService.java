package com.back.domain.job.job.service;

import com.back.domain.job.job.entity.Job;
import com.back.domain.job.job.entity.JobAlias;
import com.back.domain.job.job.repository.JobAliasRepository;
import com.back.domain.job.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService {
    private final JobRepository jobRepository;
    private final JobAliasRepository jobAliasRepository;

    public long count() {
        return jobRepository.count();
    }

    @Transactional
    public Job create(String name, String description) {
        Job job = new Job(name, description);
        return jobRepository.save(job);
    }

    @Transactional
    public JobAlias createAlias(Job job, String aliasName) {
        JobAlias alias = new JobAlias(aliasName);
        alias.setJob(job);
        return jobAliasRepository.save(alias);
    }
}
