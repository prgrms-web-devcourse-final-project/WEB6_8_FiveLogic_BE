package com.back.domain.roadmap.roadmap.service;

import com.back.domain.roadmap.roadmap.dto.response.TextFieldIntegrationResponse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextFieldIntegrationService {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    // 배치 처리 시 한 번에 처리할 노드 개수 (TPM 제한 고려)
    private static final int BATCH_SIZE = 10;

    public TextFieldIntegrationResponse integrateTextFields(List<String> learningAdvices, List<String> recommendedResources, List<String> learningGoals) {
        String systemPrompt = """
            당신은 IT 교육 컨텐츠 전문 큐레이터입니다.
            여러 멘토가 특정 주제에 대해 작성한 다양한 형식의 학습 조언, 추천 자료, 학습 목표를 입력받습니다.
            당신의 임무는 이 정보들을 종합하고 분석하여, 학습자가 이해하기 쉽고 체계적인 하나의 통합된 학습 조언, 추천 자료, 학습 목표를 생성하는 것입니다.

            결과는 반드시 다음 규칙에 맞춰 생성해야 합니다.
            1. 내용은 한국어로 작성합니다.
            2. 다른 설명이나 서론 없이, 요청된 JSON 객체만 즉시 출력합니다.
            3. JSON 객체는 반드시 다음 형식을 준수해야 합니다:
            {
                "learningAdvice": "통합되고 정제된 학습 조언 (문자열, 400자 이내)",
                "recommendedResources": "추천 자료1\\n추천 자료2\\n추천 자료3 (줄바꿈으로 구분된 문자열)",
                "learningGoals": "- 목표1\\n- 목표2\\n- 목표3 (목표 리스트만)"
            }
            4. 각 필드 작성 규칙:
               - learningAdvice: 핵심 내용을 충분히 설명, 400자 이내로 작성
               - recommendedResources: 각 자료를 줄바꿈(\\n)으로 구분, 최대 5개까지
               - learningGoals: 목표 리스트만 작성 (각 목표 앞에 "- " 붙이고 줄바꿈으로 구분, 최대 5개)
            5. 입력 데이터가 없거나 불충분한 경우, 해당 필드에는 "정보 없음"이라고 기재합니다.
            """;

        StringBuilder userPromptBuilder = new StringBuilder();
        userPromptBuilder.append("다음은 여러 멘토가 작성한 학습 조언, 추천 자료, 학습 목표 데이터입니다.");
        userPromptBuilder.append("각 항목을 분석하고 종합하여 system prompt에 명시된 JSON 형식에 맞게 통합된 결과를 생성하세요.\n\n");

        userPromptBuilder.append("--- 학습 조언 목록 ---\n");
        appendUniqueTexts(userPromptBuilder, learningAdvices);

        userPromptBuilder.append("\n--- 추천 자료 목록 ---\n");
        appendUniqueTexts(userPromptBuilder, recommendedResources);

        userPromptBuilder.append("\n--- 학습 목표 목록 ---\n");
        appendUniqueTexts(userPromptBuilder, learningGoals);

        String userPrompt = userPromptBuilder.toString();

        TextFieldIntegrationResponse response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(TextFieldIntegrationResponse.class);

        log.info("통합된 텍스트 필드 응답: {}", response);

        return response;
    }

    /**
     * 여러 노드의 텍스트 필드를 배치로 통합합니다.
     *
     * @param nodeTextsMap key: 노드 키, value: NodeTextData (advices, resources, goals)
     * @return key: 노드 키, value: 통합된 TextFieldIntegrationResponse
     */
    public Map<String, TextFieldIntegrationResponse> integrateBatch(Map<String, NodeTextData> nodeTextsMap) {
        if (nodeTextsMap == null || nodeTextsMap.isEmpty()) {
            return Collections.emptyMap();
        }

        log.info("배치 텍스트 통합 시작: 총 {}개 노드", nodeTextsMap.size());

        Map<String, TextFieldIntegrationResponse> results = new HashMap<>();
        List<Map.Entry<String, NodeTextData>> entries = new ArrayList<>(nodeTextsMap.entrySet());

        // BATCH_SIZE개씩 나눠서 처리
        for (int i = 0; i < entries.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, entries.size());
            List<Map.Entry<String, NodeTextData>> batch = entries.subList(i, endIndex);

            // 첫 배치가 아니면 TPM 제한 준수를 위해 1분 대기
            if (i > 0) {
                try {
                    log.info("TPM 제한 준수를 위해 60초 대기...");
                    Thread.sleep(60000);  // 1분 (60000ms)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("배치 간 대기 중 인터럽트 발생", e);
                }
            }

            log.info("배치 처리 중: {}-{}/{}", i + 1, endIndex, entries.size());

            try {
                Map<String, TextFieldIntegrationResponse> batchResults = processBatch(batch);
                results.putAll(batchResults);
            } catch (Exception e) {
                log.error("배치 처리 실패: {}-{}, 개별 처리로 폴백 (TPM 제한 준수를 위해 딜레이 추가)", i + 1, endIndex);
                log.error("배치 실패 원인: {}", e.getMessage());

                // 실패한 배치는 개별 처리로 폴백 (TPM 초과 방지를 위해 딜레이 추가)
                for (int j = 0; j < batch.size(); j++) {
                    Map.Entry<String, NodeTextData> entry = batch.get(j);

                    try {
                        // TPM 제한 준수: 개별 처리 간 6초 대기 (10개 × 6초 = 60초)
                        if (j > 0) {
                            log.debug("개별 폴백 TPM 제한 준수: 6초 대기 ({}/{})", j + 1, batch.size());
                            Thread.sleep(6000);
                        }

                        NodeTextData data = entry.getValue();
                        TextFieldIntegrationResponse response = integrateTextFields(
                                data.advices(),
                                data.resources(),
                                data.goals()
                        );
                        results.put(entry.getKey(), response);
                        log.debug("개별 폴백 성공: key={} ({}/{})", entry.getKey(), j + 1, batch.size());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("개별 폴백 대기 중 인터럽트 발생: key={}", entry.getKey());
                        results.put(entry.getKey(), new TextFieldIntegrationResponse(null, null, null));
                    } catch (Exception ex) {
                        log.error("개별 처리도 실패: key={}, 에러={}", entry.getKey(), ex.getMessage());
                        // 최후의 폴백: 빈 응답
                        results.put(entry.getKey(), new TextFieldIntegrationResponse(null, null, null));
                    }
                }
                log.info("개별 폴백 처리 완료: {}개 노드", batch.size());
            }
        }

        log.info("배치 텍스트 통합 완료: {}개 노드 처리", results.size());
        return results;
    }

    /**
     * 한 배치를 AI에게 전송하여 처리
     */
    private Map<String, TextFieldIntegrationResponse> processBatch(List<Map.Entry<String, NodeTextData>> batch) {
        String systemPrompt = """
            당신은 IT 교육 컨텐츠 전문 큐레이터입니다.
            여러 노드에 대해 각각의 학습 조언, 추천 자료, 학습 목표를 통합해야 합니다.

            결과는 반드시 다음 규칙에 맞춰 생성해야 합니다:
            1. 내용은 한국어로 작성합니다.
            2. 다른 설명이나 서론 없이, 요청된 JSON 객체만 즉시 출력합니다.
            3. JSON 객체는 다음 형식의 배열이어야 합니다:
            [
              {
                "nodeKey": "노드 키",
                "learningAdvice": "통합되고 정제된 학습 조언 (문자열, 400자 이내)",
                "recommendedResources": "추천 자료1\\n추천 자료2\\n추천 자료3 (줄바꿈으로 구분된 문자열)",
                "learningGoals": "- 목표1\\n- 목표2\\n- 목표3 (목표 리스트만)"
              },
              ...
            ]
            4. 각 필드 작성 규칙:
               - learningAdvice: 핵심 내용을 충분히 설명, 400자 이내로 작성
               - recommendedResources: 각 자료를 줄바꿈(\\n)으로 구분, 최대 5개까지
               - learningGoals: 목표 리스트만 작성 (각 목표 앞에 "- " 붙이고 줄바꿈으로 구분, 최대 5개)
            5. **중요**: 모든 필드는 문자열(String) 타입이어야 합니다. 배열이 아닙니다.
            6. 입력 데이터가 없거나 불충분한 경우, 해당 필드에는 null을 기재합니다.
            7. 모든 노드에 대해 반드시 응답을 생성하세요.
            """;

        StringBuilder userPromptBuilder = new StringBuilder();
        userPromptBuilder.append("다음은 여러 노드의 학습 조언, 추천 자료, 학습 목표 데이터입니다.\n");
        userPromptBuilder.append("각 노드별로 데이터를 통합하여 system prompt에 명시된 JSON 배열 형식으로 응답하세요.\n\n");

        for (int i = 0; i < batch.size(); i++) {
            Map.Entry<String, NodeTextData> entry = batch.get(i);
            String nodeKey = entry.getKey();
            NodeTextData data = entry.getValue();

            userPromptBuilder.append(String.format("=== 노드 %d: %s ===\n", i + 1, nodeKey));

            // 중복 제거 후 추가
            userPromptBuilder.append("--- 학습 조언 목록 ---\n");
            appendUniqueTexts(userPromptBuilder, data.advices());

            userPromptBuilder.append("\n--- 추천 자료 목록 ---\n");
            appendUniqueTexts(userPromptBuilder, data.resources());

            userPromptBuilder.append("\n--- 학습 목표 목록 ---\n");
            appendUniqueTexts(userPromptBuilder, data.goals());

            userPromptBuilder.append("\n");
        }

        String userPrompt = userPromptBuilder.toString();

        // 프롬프트 길이 로깅 (모니터링용)
        int promptLength = userPrompt.length();
        log.debug("배치 프롬프트 길이: {} 문자 (약 {} 토큰)", promptLength, promptLength / 4);

        // AI에게 요청
        String responseJson = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        log.debug("배치 AI 응답: {}", responseJson);

        // JSON 파싱
        return parseBatchResponse(responseJson, batch);
    }

    /**
     * 중복을 제거한 텍스트 목록을 StringBuilder에 추가
     */
    private void appendUniqueTexts(StringBuilder builder, List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            builder.append("데이터가 없습니다.\n");
            return;
        }

        // 중복 제거 (순서 유지)
        Set<String> seen = new LinkedHashSet<>();
        for (String text : texts) {
            if (text != null && !text.isBlank()) {
                seen.add(text.trim());
            }
        }

        if (seen.isEmpty()) {
            builder.append("데이터가 없습니다.\n");
        } else {
            seen.forEach(text -> builder.append("- ").append(text).append("\n"));
        }
    }

    /**
     * AI 응답 JSON을 파싱하여 Map으로 변환
     */
    private Map<String, TextFieldIntegrationResponse> parseBatchResponse(
            String responseJson,
            List<Map.Entry<String, NodeTextData>> batch
    ) {
        try {
            // JSON 배열 파싱
            List<BatchResponseItem> items = objectMapper.readValue(
                    responseJson,
                    new TypeReference<List<BatchResponseItem>>() {}
            );

            // 응답 검증: 요청한 노드 키 수집
            Set<String> requestedKeys = batch.stream()
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            Map<String, TextFieldIntegrationResponse> results = new HashMap<>();
            Set<String> respondedKeys = new HashSet<>();

            for (BatchResponseItem item : items) {
                String nodeKey = item.nodeKey();
                respondedKeys.add(nodeKey);

                // 요청하지 않은 키가 응답에 포함된 경우 경고
                if (!requestedKeys.contains(nodeKey)) {
                    log.warn("AI가 요청하지 않은 노드 키를 반환했습니다: {}", nodeKey);
                    continue;
                }

                results.put(
                        nodeKey,
                        new TextFieldIntegrationResponse(
                                item.learningAdvice(),
                                item.recommendedResources(),
                                item.learningGoals()
                        )
                );
            }

            // 누락된 노드 확인
            Set<String> missingKeys = new HashSet<>(requestedKeys);
            missingKeys.removeAll(respondedKeys);

            if (!missingKeys.isEmpty()) {
                log.warn("AI가 응답하지 않은 노드가 있습니다: {} (총 {}개)", missingKeys, missingKeys.size());
                // 누락된 노드는 빈 응답으로 채움
                for (String missingKey : missingKeys) {
                    results.put(missingKey, new TextFieldIntegrationResponse(null, null, null));
                }
            }

            // 최종 검증: 모든 요청 노드가 결과에 포함되었는지 확인
            if (results.size() != requestedKeys.size()) {
                log.error("응답 개수 불일치: 요청 {}개, 응답 {}개", requestedKeys.size(), results.size());
            }

            return results;
        } catch (Exception e) {
            log.error("배치 응답 파싱 실패: {}, 응답 내용: {}", e.getMessage(), responseJson);
            throw new RuntimeException("AI 응답 파싱 실패", e);
        }
    }

    /**
     * 노드 하나의 텍스트 데이터
     */
    public record NodeTextData(
            List<String> advices,
            List<String> resources,
            List<String> goals
    ) {}

    /**
     * AI 배치 응답 파싱용 DTO
     */
    private static class BatchResponseItem {
        private String nodeKey;
        private String learningAdvice;

        @JsonDeserialize(using = StringOrArrayDeserializer.class)
        private String recommendedResources;

        @JsonDeserialize(using = StringOrArrayDeserializer.class)
        private String learningGoals;

        public String nodeKey() { return nodeKey; }
        public String learningAdvice() { return learningAdvice; }
        public String recommendedResources() { return recommendedResources; }
        public String learningGoals() { return learningGoals; }

        public void setNodeKey(String nodeKey) { this.nodeKey = nodeKey; }
        public void setLearningAdvice(String learningAdvice) { this.learningAdvice = learningAdvice; }
        public void setRecommendedResources(String recommendedResources) { this.recommendedResources = recommendedResources; }
        public void setLearningGoals(String learningGoals) { this.learningGoals = learningGoals; }
    }

    /**
     * String 또는 Array를 String으로 변환하는 커스텀 디시리얼라이저
     * AI가 배열로 반환해도 줄바꿈으로 구분된 문자열로 변환
     */
    private static class StringOrArrayDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);

            if (node.isNull()) {
                return null;
            }

            if (node.isTextual()) {
                return node.asText();
            }

            if (node.isArray()) {
                // 배열인 경우 줄바꿈으로 구분된 문자열로 변환
                List<String> items = new ArrayList<>();
                for (JsonNode item : node) {
                    if (!item.isNull()) {
                        items.add(item.asText());
                    }
                }
                return String.join("\n", items);
            }

            // 기타 타입은 문자열로 변환
            return node.toString();
        }
    }
}
