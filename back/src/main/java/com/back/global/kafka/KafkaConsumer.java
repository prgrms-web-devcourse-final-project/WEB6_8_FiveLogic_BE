package com.back.global.kafka;

import com.back.domain.file.video.service.FileManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {
    private final FileManager fileManager;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "transcoding-status")
    public void consume(String transcodingStatusMessage) {

        try {
            JsonNode rootNode = mapper.readTree(transcodingStatusMessage);

            String key = rootNode.get("key").asText();
            String uuid = key.replaceFirst("^videos/", "");

            String status = rootNode.get("qualities").toString();
            //현재 duration이 0인 문제가 있음 영상 길이가 필요한게 아니라면 제거할 예정
            fileManager.updateVideoStatus(uuid, status);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
