package com.back.domain.roadmap.roadmap.service;

import com.back.domain.job.job.entity.Job;
import com.back.domain.job.job.repository.JobRepository;
import com.back.domain.roadmap.roadmap.entity.JobRoadmap;
import com.back.domain.roadmap.roadmap.entity.JobRoadmapNodeStat;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode;
import com.back.domain.roadmap.roadmap.entity.RoadmapNode.RoadmapType;
import com.back.domain.roadmap.roadmap.entity.MentorRoadmap;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapNodeStatRepository;
import com.back.domain.roadmap.roadmap.repository.JobRoadmapRepository;
import com.back.domain.roadmap.roadmap.repository.MentorRoadmapRepository;
import com.back.domain.roadmap.task.entity.Task;
import com.back.domain.roadmap.task.repository.TaskRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobRoadmapIntegrationService {
    private final MentorRoadmapRepository mentorRoadmapRepository;
    private final JobRepository jobRepository;
    private final JobRoadmapRepository jobRoadmapRepository;
    private final TaskRepository taskRepository;
    private final JobRoadmapNodeStatRepository jobRoadmapNodeStatRepository;

    private final double BRANCH_THRESHOLD = 0.25;
    private final int MAX_DEPTH = 6;
    private final int MAX_CHILDREN = 6;

    @Transactional
    public JobRoadmap integrateJobRoadmap(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ServiceException("404", "직업을 찾을 수 없습니다. id=" + jobId));

        // 옵션: 기존 JobRoadmap 삭제
        jobRoadmapRepository.findByJob(job).ifPresent(existing -> {
            jobRoadmapRepository.delete(existing);
            log.info("기존 JobRoadmap 삭제: id={}", existing.getId());
        });

        List<MentorRoadmap> mentorRoadmaps = mentorRoadmapRepository.findAllByMentorJobIdWithNodes(jobId);
        if (mentorRoadmaps.isEmpty()) {
            throw new ServiceException("404", "해당 직업에 대한 멘토 로드맵이 존재하지 않습니다.");
        }
        final int totalMentorCount = mentorRoadmaps.size();

        // 집계 자료구조
        Map<String, AggregatedNode> agg = new HashMap<>();
        Map<String, Map<String, Integer>> transitions = new HashMap<>();
        Map<String, Integer> rootCount = new HashMap<>();
        Map<String, Set<Long>> mentorAppearSet = new HashMap<>(); // unique mentors per key
        Map<String, List<Integer>> positions = new HashMap<>(); // to compute average position
        Map<String, List<String>> descs = new HashMap<>();

        // 노드를 식별하기 위한 키 생성(Task와 연결되어 있으면 taskId로, 없으면 taskName으로)
        java.util.function.Function<RoadmapNode, String> keyFunc = rn -> {
            if (rn.getTask() != null) return "T:" + rn.getTask().getId();
            return "N:" + (rn.getTaskName() == null ? "" : rn.getTaskName().trim().toLowerCase());
        };

        // 멘토 로드맵의 노드를 순서대로 정렬 (stepOrder 기준)
        for (MentorRoadmap mr : mentorRoadmaps) {
            List<RoadmapNode> nodes = mr.getNodes().stream()
                    .sorted(Comparator.comparingInt(RoadmapNode::getStepOrder))
                    .toList();

            if (nodes.isEmpty()) continue;

            // 첫번째 노드를 root 후보로 등록
            RoadmapNode first = nodes.get(0);
            String firstKey = keyFunc.apply(first);
            rootCount.merge(firstKey, 1, Integer::sum);

            // 멘토 ID를 저장 (노드별 mentorAppearSet에 활용)
            Long mentorId = mr.getMentor().getId();

            // 각 노드 집계
            for (int i = 0; i < nodes.size(); i++) {
                RoadmapNode rn = nodes.get(i);
                String k = keyFunc.apply(rn);

                // agg: 노드별 출현 횟수
                AggregatedNode an = agg.computeIfAbsent(k, kk -> new AggregatedNode(rn.getTask(), rn.getTask() != null ? rn.getTask().getName() : rn.getTaskName()));
                an.count += 1;

                // positions: 현재 위치 기록
                positions.computeIfAbsent(k, kk -> new ArrayList<>()).add(i + 1);

                // metorAppearSet: 멘토 등장 기록
                mentorAppearSet.computeIfAbsent(k, kk -> new HashSet<>()).add(mentorId);

                // descs: description 모으기
                if (rn.getDescription() != null && !rn.getDescription().isBlank()) {
                    descs.computeIfAbsent(k, kk -> new ArrayList<>()).add(rn.getDescription());
                }

                // transitions: 다음 노드로의 이동 기록 (A->B가 몇 번 나왔는지)
                if (i < nodes.size() - 1) {
                    String nextKey = keyFunc.apply(nodes.get(i + 1));
                    transitions.computeIfAbsent(k, kk -> new HashMap<>()).merge(nextKey, 1, Integer::sum);
                }
            }
        }

        // Task를 prefetch (통합된 결과를 만들 때 필요한 task들 한번에 prefetch)
        Set<Long> taskIds = agg.values().stream()
                .map(a -> a.task != null ? a.task.getId() : null)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Task> taskMap = new HashMap<>();
        if (!taskIds.isEmpty()) taskRepository.findAllById(taskIds).forEach(t -> taskMap.put(t.getId(), t));

        // Root 노드 선택: Root 후보 중 가장 많이 쓰인 것을 선택
        String rootKey = rootCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseGet(() -> agg.entrySet().stream().max(Comparator.comparingInt(e -> e.getValue().count)).map(Map.Entry::getKey).orElseThrow());

        // 노드 생성 (집계된 데이터를 RoadmapNode 엔티티로 변환)
        Map<String, RoadmapNode> keyToNode = new HashMap<>();
        for (Map.Entry<String, AggregatedNode> e : agg.entrySet()) {
            AggregatedNode a = e.getValue();
            RoadmapNode node = RoadmapNode.builder()
                    .taskName(a.displayName)
                    .description(mergeTopDescriptions(descs.get(e.getKey())))
                    .task(a.task != null ? taskMap.get(a.task.getId()) : null)
                    .stepOrder(0) // assign later
                    .level(0)     // assign later via addChild/setLevel
                    .roadmapId(0L)
                    .roadmapType(RoadmapType.JOB)
                    .build();
            keyToNode.put(e.getKey(), node);
        }

        // Transition 빈도 기반 자식 선택
        Map<String, List<String>> chosenChildren = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> e : transitions.entrySet()) {
            String parent = e.getKey();
            Map<String, Integer> cmap = e.getValue();
            int parentCount = agg.getOrDefault(parent, new AggregatedNode(null, parent)).count;
            List<Map.Entry<String, Integer>> sorted = cmap.entrySet().stream()
                    .sorted((x, y) -> y.getValue().compareTo(x.getValue()))
                    .limit(MAX_CHILDREN)
                    .toList();

            List<String> chosen = new ArrayList<>();
            for (int i = 0; i < sorted.size(); i++) {
                Map.Entry<String, Integer> ce = sorted.get(i);
                if (i == 0) chosen.add(ce.getKey());
                else {
                    double ratio = parentCount == 0 ? 0.0 : (double) ce.getValue() / parentCount;
                    if (ratio >= BRANCH_THRESHOLD) chosen.add(ce.getKey());
                }
            }
            if (!chosen.isEmpty()) chosenChildren.put(parent, chosen);
        }

        // BFS 탐색을 통해 parent-child 연결 생성
        if (!keyToNode.containsKey(rootKey)) {
            // fallback root 생성
            AggregatedNode a = agg.get(rootKey);
            RoadmapNode rootNode = RoadmapNode.builder()
                    .taskName(a != null ? a.displayName : "root")
                    .description(mergeTopDescriptions(descs.get(rootKey)))
                    .task(a != null ? a.task : null)
                    .stepOrder(0)
                    .level(0)
                    .roadmapId(0L)
                    .roadmapType(RoadmapType.JOB)
                    .build();
            keyToNode.put(rootKey, rootNode);
        }
        RoadmapNode rootNode = keyToNode.get(rootKey);

        // BFS queue
        Queue<String> q = new ArrayDeque<>();
        q.add(rootKey);

        // 형제 순서대로 stepOrder 지정
        while (!q.isEmpty()) {
            String pk = q.poll();
            RoadmapNode parentNode = keyToNode.get(pk);
            List<String> childs = chosenChildren.getOrDefault(pk, Collections.emptyList());
            int order = 1;
            for (String ck : childs) {
                RoadmapNode childNode = keyToNode.get(ck);
                if (childNode == null) continue;
                parentNode.addChild(childNode); // addChild에서 parent, level 설정
                childNode.setStepOrder(order++);
                q.add(ck);
            }
        }

        // 루트 노드가 여러 개일 경우 등장 횟수 순으로 정렬해 stepOrder 부여
        List<RoadmapNode> topLevelNodes = keyToNode.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(n -> n.getParent() == null)
                .sorted((n1, n2) -> {
                    String k1 = keyOf(n1);
                    String k2 = keyOf(n2);
                    int c1 = agg.getOrDefault(k1, new AggregatedNode(null, k1)).count;
                    int c2 = agg.getOrDefault(k2, new AggregatedNode(null, k2)).count;
                    return Integer.compare(c2, c1);
                })
                .toList();

        int rootOrder = 1;
        for (RoadmapNode tln : topLevelNodes) {
            tln.setStepOrder(rootOrder++);
            tln.setLevel(0);
        }

        // JobRoadmap 생성
        JobRoadmap jobRoadmap = JobRoadmap.builder().job(job).build();

        // 모든 노드 level, stepOrder에 따라 정렬
        List<RoadmapNode> allNodes = keyToNode.values().stream()
                .sorted(Comparator.comparingInt(RoadmapNode::getLevel).thenComparingInt(RoadmapNode::getStepOrder))
                .toList();

        // jobRoadmap 저장해 ID 받아옴
        jobRoadmap = jobRoadmapRepository.save(jobRoadmap);
        Long roadmapId = jobRoadmap.getId();

        // 모든 노드에 roadmapId, roadmapType 설정 후 JobRoadmap의 노드로 추가
        for (RoadmapNode n : allNodes) {
            n.setRoadmapId(roadmapId);
            n.setRoadmapType(RoadmapType.JOB);
            jobRoadmap.getNodes().add(n);
        }

        JobRoadmap saved = jobRoadmapRepository.save(jobRoadmap);

        // 노드별 통계값 저장
        List<JobRoadmapNodeStat> stats = new ArrayList<>();
        for (RoadmapNode persisted : saved.getNodes()) {
            String k = keyOfPersisted(persisted);
            AggregatedNode a = agg.get(k);
            JobRoadmapNodeStat stat = new JobRoadmapNodeStat();
            stat.setNode(persisted);
            stat.setStepOrder(persisted.getStepOrder());
            double weight = a == null ? 0.0 : (double) a.count / (double) totalMentorCount;
            stat.setWeight(weight);

            // 해당 키를 선택한 멘토 수
            int mentorCount = mentorAppearSet.getOrDefault(k, Collections.emptySet()).size();
            stat.setMentorCount(mentorCount);

            // 평균 단계 위치
            List<Integer> posList = positions.getOrDefault(k, Collections.emptyList());
            double avgPos = posList.isEmpty() ? null : posList.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            stat.setAveragePosition(posList.isEmpty() ? null : avgPos);

            // 해당 키에서 나가는 transition 개수
            int outgoing = transitions.getOrDefault(k, Collections.emptyMap()).values().stream().mapToInt(Integer::intValue).sum();
            // 해당 키로 들어오는 transition 개수
            int incoming = transitions.entrySet().stream()
                    .mapToInt(e -> e.getValue().getOrDefault(k, 0)).sum();

            stat.setOutgoingTransitions(outgoing);
            stat.setIncomingTransitions(incoming);

            // 다음으로 이어지는 노드들의 분포
            Map<String, Integer> outMap = transitions.getOrDefault(k, Collections.emptyMap());
            if (!outMap.isEmpty()) {
                // JSON 형태로 저장
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                boolean first = true;
                for (Map.Entry<String, Integer> ee : outMap.entrySet()) {
                    if (!first) sb.append(",");
                    sb.append("\"").append(escapeJson(ee.getKey())).append("\":").append(ee.getValue());
                    first = false;
                }
                sb.append("}");
                stat.setTransitionCounts(sb.toString());
            }

            stats.add(stat);
        }

        jobRoadmapNodeStatRepository.saveAll(stats);
        log.info("JobRoadmap created id={} nodes={} stats={}", saved.getId(), saved.getNodes().size(), stats.size());

        return saved;
    }

    // ---------------- 헬퍼 클래스&메서드 ----------------

    private static class AggregatedNode {
        Task task;
        String displayName;
        int count = 0;
        AggregatedNode(Task task, String displayName) { this.task = task; this.displayName = displayName; }
    }

    private String mergeTopDescriptions(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String s : list) {
            if (s == null) continue;
            String c = s.trim();
            if (!c.isEmpty()) set.add(c);
            if (set.size() >= 3) break;
        }
        return String.join("\n\n", set);
    }

    private String keyOf(RoadmapNode rn) {
        Task t = rn.getTask();
        if (t != null) return "T:" + t.getId();
        return "N:" + (rn.getTaskName() == null ? "" : rn.getTaskName().trim().toLowerCase());
    }

    private String keyOfPersisted(RoadmapNode rn) {
        Task t = rn.getTask();
        if (t != null) return "T:" + t.getId();
        return "N:" + (rn.getTaskName() == null ? "" : rn.getTaskName().trim().toLowerCase());
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
