package com.back.domain.job.job.repository;

import com.back.domain.job.job.entity.JobAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobAliasRepository extends JpaRepository<JobAlias, Long> {
    Optional<JobAlias> findByName(String name);
    Optional<JobAlias> findByNameIgnoreCase(String name);
    List<JobAlias> findByJobIsNull(); // pending 상태인 alias 조회
}
