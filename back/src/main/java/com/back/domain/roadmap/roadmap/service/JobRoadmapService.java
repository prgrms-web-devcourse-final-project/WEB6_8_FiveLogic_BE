package com.back.domain.roadmap.roadmap.service;

import com.back.domain.roadmap.roadmap.dto.response.JobRoadmapListResponse;
import com.back.domain.roadmap.roadmap.dto.response.JobRoadmapResponse;
import com.back.domain.roadmap.roadmap.entity.JobRoadmap;
import com.back.domain.roadmap.roadmap.entity.JobRoadmapNodeStat;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapNodeStatRepository;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class JobRoadmapService {
    private final JobRoadmapRepository jobRoadmapRepository;
    private final JobRoadmapNodeStatRepository jobRoadmapNodeStatRepository;

    public List<JobRoadmapListResponse> getAllJobRoadmaps() {
        return jobRoadmapRepository.findAllWithJob()
                .stream()
                .map(this::toListResponse)
                .toList();
    }

    public Page<JobRoadmapListResponse> getJobRoadmaps(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return jobRoadmapRepository.findAllWithJobAndKeyword(keyword, pageable)
                .map(this::toListResponse);
    }

    public JobRoadmapResponse getJobRoadmapById(Long id) {
        JobRoadmap jobRoadmap = jobRoadmapRepository.findByIdWithJobAndNodes(id)
                .orElseThrow(() -> new ServiceException("404", "직업 로드맵을 찾을 수 없습니다."));

        // 통계 정보 조회 및 Map 생성 (nodeId -> JobRoadmapNodeStat)
        List<JobRoadmapNodeStat> stats = jobRoadmapNodeStatRepository.findByNode_RoadmapIdWithNode(id);
        Map<Long, JobRoadmapNodeStat> statMap = stats.stream()
                .collect(Collectors.toMap(stat -> stat.getNode().getId(), stat -> stat));

        return JobRoadmapResponse.from(jobRoadmap, jobRoadmap.getJob().getName(), statMap);
    }

    private JobRoadmapListResponse toListResponse(JobRoadmap jobRoadmap) {
        return JobRoadmapListResponse.of(
                jobRoadmap.getId(),
                jobRoadmap.getJob().getName(),
                jobRoadmap.getJob().getDescription()
        );
    }
}